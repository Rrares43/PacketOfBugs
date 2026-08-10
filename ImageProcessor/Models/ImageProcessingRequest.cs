namespace ImageProcessor.Models
{
    public class ImageProcessingRequest
    {
        public string DownloadUrl { get; set; }
        public string UploadUrl { get; set; }
        public string Filter { get; set; }
    }
}
