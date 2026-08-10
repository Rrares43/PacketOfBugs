using ImageProcessor.Filters;

namespace ImageProcessor.Services
{
    public class FilterListService
    {
        private static readonly List<FilterDetails> _filters = new List<FilterDetails>
        {
            new FilterDetails { Id = (int)FilterType.None, Name = "none", Label = "No Filter" },
            new FilterDetails { Id = (int)FilterType.Grayscale, Name = "grayscale", Label = "Black and White" },
            new FilterDetails {Id = (int)FilterType.Invert, Name = "invert", Label = "Invert colors"},
            new FilterDetails {Id=(int)FilterType.Sepia, Name="sepia", Label="Warm Brown Style" },
            new FilterDetails {Id=(int)FilterType.Neon, Name="neon", Label="Neon/Cyberpunk Style"}
        };

        public List<FilterDetails> GetFilters()
        {
            return _filters;
        }
    }
}
