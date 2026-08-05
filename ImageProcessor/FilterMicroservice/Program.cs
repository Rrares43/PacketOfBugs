using System.Drawing;
using System.Drawing.Imaging;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

app.MapPost("/api/images/grayscale", async (IFormFile file) =>
{
    if (file == null || file.Length == 0)
    {
        return Results.BadRequest("No image provided.");
    }

    using var memoryStream = new MemoryStream();
    await file.CopyToAsync(memoryStream);
    memoryStream.Position = 0;

    using var originalBitmap = new Bitmap(memoryStream);
    using var grayscaleBitmap = new Bitmap(originalBitmap.Width, originalBitmap.Height);

    for (int y = 0; y < originalBitmap.Height; y++)
    {
        for (int x = 0; x < originalBitmap.Width; x++)
        {
            Color originalColor = originalBitmap.GetPixel(x, y);

            int grayColor = (int)(originalColor.R * 0.3 + originalColor.G * 0.59 + originalColor.B * 0.11);

            Color newColor = Color.FromArgb(originalColor.A, grayColor, grayColor, grayColor);
            grayscaleBitmap.SetPixel(x, y, newColor);
        }
    }

    using var outputStream = new MemoryStream();
    grayscaleBitmap.Save(outputStream, ImageFormat.Jpeg);
    outputStream.Position = 0;

    return Results.File(outputStream.ToArray(), "image/jpeg");
});

app.Run();