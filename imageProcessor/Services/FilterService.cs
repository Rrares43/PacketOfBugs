using ImageProcessor.Filters;
using ImageProcessor.Models;
using SixLabors.ImageSharp;
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

        public async Task ProcessAndUploadImageAsync(ImageProcessingRequest request)
        {
            if (!Enum.TryParse<FilterType>(request.Filter, true, out var filterType))
            {
                throw new ArgumentException($"Filter {request.Filter} is not implemented.");
            }

            using var networkStream = await _httpClient.GetStreamAsync(request.DownloadUrl);

            using var memStream = new MemoryStream();
            await networkStream.CopyToAsync(memStream);
            memStream.Position = 0;

            var originalFormat = await Image.DetectFormatAsync(memStream);
            memStream.Position = 0;

            using var image = await Image.LoadAsync(memStream);

            var stopwatch = Stopwatch.StartNew();

            ApplyFilter(image, filterType);

            Console.WriteLine($"[Performanță] Filtrul {filterType} aplicat în: {stopwatch.ElapsedMilliseconds} ms");

            using var outStream = new MemoryStream();
            await image.SaveAsync(outStream, originalFormat);

            byte[] processedBytes = outStream.ToArray();
            using var content = new ByteArrayContent(processedBytes);
            content.Headers.ContentType = new MediaTypeHeaderValue(originalFormat.DefaultMimeType);

            var response = await _httpClient.PutAsync(request.UploadUrl, content);
            response.EnsureSuccessStatusCode();
        }

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

                     
                        int scaleFactor = 24;

                        int gridWidth = Math.Max(8, origWidth / scaleFactor);
                        int gridHeight = Math.Max(8, origHeight / scaleFactor);

                        image.Mutate(x => x
                            .Resize(new ResizeOptions
                            {
                                Size = new Size(gridWidth, gridHeight),
                                Sampler = KnownResamplers.NearestNeighbor
                            })
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