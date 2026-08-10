using ImageProcessor.Filters;
using ImageProcessor.Models;
using Microsoft.AspNetCore.Server.Kestrel.Core;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Formats.Png;
using SixLabors.ImageSharp.PixelFormats;
using System.Diagnostics.CodeAnalysis;
using System.Net.Http.Headers;

namespace ImageProcessor.Service
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

            using var image = await Image.LoadAsync<Rgba32>(memStream);
            var pixelData = new byte[image.Width * image.Height * 4];
            image.CopyPixelDataTo(pixelData);

            ApplyFilter(pixelData, filterType);

            using var finalImage = Image.LoadPixelData<Rgba32>(pixelData, image.Width, image.Height);
            using var outStream = new MemoryStream();

            await finalImage.SaveAsync(outStream, originalFormat);

            byte[] processedBytes = outStream.ToArray();

            using var content = new ByteArrayContent(processedBytes);

            content.Headers.ContentType = new MediaTypeHeaderValue(originalFormat.DefaultMimeType);

            var response = await _httpClient.PutAsync(request.UploadUrl, content);

            response.EnsureSuccessStatusCode();
        }

        private void ApplyGrayscale(byte[] pixels)
        {
            for (int i = 0; i < pixels.Length; i += 4)
            {
                byte gray = (byte)((pixels[i] * 0.299) + (pixels[i + 1] * 0.587) + (pixels[i + 2] * 0.114));
                pixels[i] = gray;
                pixels[i + 1] = gray;
                pixels[i + 2] = gray;
            }
        }

        private void ApplyInvert(byte[] pixels)
        {
            for (int i = 0; i < pixels.Length; i += 4)
            {
                pixels[i] = (byte)(255 - pixels[i]);
                pixels[i + 1] = (byte)(255 - pixels[i + 1]);
                pixels[i + 2] = (byte)(255 - pixels[i + 2]);
            }
        }

        private void ApplySepia(byte[]pixels)
        {
            for (int i = 0; i < pixels.Length; i += 4)
            {
                byte r = pixels[i];
                byte g = pixels[i + 1];
                byte b = pixels[i + 2];

               
                int newR = (int)(0.393 * r + 0.769 * g + 0.189 * b);
                int newG = (int)(0.349 * r + 0.686 * g + 0.168 * b);
                int newB = (int)(0.272 * r + 0.534 * g + 0.131 * b);

                pixels[i] = (byte)Math.Min(255, newR); 
                pixels[i + 1] = (byte)Math.Min(255, newG); 
                pixels[i + 2] = (byte)Math.Min(255, newB); 
            }
        }

        private void ApplyNeon(byte[] pixels)
        {
            
                for (int i = 0; i < pixels.Length; i += 4)
                {
                    float r = pixels[i];
                    float g = pixels[i + 1];
                    float b = pixels[i + 2];

                float lum = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
                float smoothLum = lum * lum * (3.0f - 2.0f * lum);

                float finalR = 10 + smoothLum * (255 - 10);
                float finalG = 150 * (1.0f - smoothLum) + 20 * smoothLum;
                float finalB = 230 * (1.0f - smoothLum) + 220 * smoothLum;

                float intensity = Math.Min(1.0f, lum * 2.0f);

                pixels[i] = (byte)Math.Clamp(finalR * intensity, 0, 255);
                pixels[i + 1] = (byte)Math.Clamp(finalG * intensity, 0, 255);
                pixels[i + 2] = (byte)Math.Clamp(finalB * intensity, 0, 255);
            }
            
        }

        private void ApplyFilter(byte[] pixels, FilterType type)
        {
            switch (type)
            {
                case FilterType.Grayscale:
                    ApplyGrayscale(pixels);
                    break;

                case FilterType.Invert:
                    ApplyInvert(pixels);
                    break;

                case FilterType.Sepia:
                    ApplySepia(pixels);
                    break;

                case FilterType.Neon:
                    ApplyNeon(pixels);
                    break;  
                   
            }
        }
    }
}