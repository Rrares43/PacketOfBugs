using ImageProcessor.Filters;
using ImageProcessor.Models;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Formats;
using SixLabors.ImageSharp.Processing;
using System.Diagnostics;
using System.Net.Http.Headers;

using Image = SixLabors.ImageSharp.Image;

namespace ImageProcessor.Services
{
    public class FilterService
    {
        private readonly HttpClient _httpClient;

        public FilterService(HttpClient httpClient)
        {
            _httpClient = httpClient;
        }

        
        /// <summary>
        /// Downloads image, processes it and uploads the result.
        /// </summary>
        public async Task ProcessAndUploadImageAsync(ImageProcessingRequest request)
        {
            if (!Enum.TryParse<FilterType>(request.Filter, true, out var filterType))
            {
                throw new ArgumentException($"Filter {request.Filter} is not implemented.");
            }

            // download image stream
            using var networkStream = await _httpClient.GetStreamAsync(request.DownloadUrl);
            using var memStream = new MemoryStream();
            await networkStream.CopyToAsync(memStream);
            byte[] inputBytes = memStream.ToArray();

            // process image bytes
            var (processedBytes, contentType) = await ProcessImageBytesAsync(inputBytes, filterType);

            // upload processed image
            using var content = new ByteArrayContent(processedBytes);
            content.Headers.ContentType = new MediaTypeHeaderValue(contentType);

            var response = await _httpClient.PutAsync(request.UploadUrl, content);
            response.EnsureSuccessStatusCode();
        }

        /// <summary>Filters an uploaded stream entirely in memory and returns its encoded bytes.</summary>
        public async Task<(byte[] Data, string MimeType)> ProcessImageAsync(Stream input, string filter)
        {
            if (!Enum.TryParse<FilterType>(filter, true, out var filterType))
            {
                throw new ArgumentException($"Filter {filter} is not implemented.");
            }

            using var memStream = new MemoryStream();
            await input.CopyToAsync(memStream);
            return await ProcessImageBytesAsync(memStream.ToArray(), filterType);
        }

        /// <summary>
        /// Processing method: takes bytes, applies filter, returns processed bytes and MIME type.
        /// </summary>
        private async Task<(byte[] Data, string MimeType)> ProcessImageBytesAsync(byte[] imageBytes, FilterType filterType)
        {
            using var inStream = new MemoryStream(imageBytes);

            // detect format to preserve original extension (JPEG, PNG, etc.)
            IImageFormat originalFormat = await Image.DetectFormatAsync(inStream);
            inStream.Position = 0;

            using var image = await Image.LoadAsync(inStream);

            var stopwatch = Stopwatch.StartNew();
            ApplyFilter(image, filterType);
            Console.WriteLine($"Filter {filterType} applied in: {stopwatch.ElapsedMilliseconds} ms");

            using var outStream = new MemoryStream();
            await image.SaveAsync(outStream, originalFormat);

            return (outStream.ToArray(), originalFormat.DefaultMimeType);
        }

        /// <summary>
        /// Private method that manages the application of graphic filters based on the selected type.
        /// </summary>
        private void ApplyFilter(Image image, FilterType type)
        {
            image.Mutate(x =>
            {
                switch (type)
                {
                    case FilterType.Grayscale:
                        x.Grayscale();
                        break;

                    case FilterType.Invert:
                        x.Invert();
                        break;

                    case FilterType.Sepia:
                        x.Sepia();
                        break;

                    case FilterType.Sketch:

                        // combines multiple effects to simulate a ink sketch effect
                        image.Mutate(x => x
                            .Grayscale()
                            .GaussianSharpen(2.5f)
                            .Contrast(2.0f)
                            .DetectEdges()
                            .Invert()
                            );
                        break;

                    case FilterType.Pixel:
                        int origWidth = image.Width;
                        int origHeight = image.Height;

                        int scaleFactor = 24; // factor controlling the pixel size

                        int gridWidth = Math.Max(8, origWidth / scaleFactor);
                        int gridHeight = Math.Max(8, origHeight / scaleFactor);

                        image.Mutate(x => x

                            // shrink the image
                            .Resize(new ResizeOptions
                            {
                                Size = new Size(gridWidth, gridHeight),
                                Sampler = KnownResamplers.NearestNeighbor
                            })

                            // enlarge it back to original size to create a pixelated effect
                            .Resize(new ResizeOptions
                            {
                                Size = new Size(origWidth, origHeight),
                                Sampler = KnownResamplers.NearestNeighbor
                            })
                            .Saturate(1.7f)
                            .Contrast(1.4f));
                        break;



                }
            });
        }

       
    }
}
