using ImageProcessor.Filters;
using ImageProcessor.Models;
using ImageProcessor.Services;
using Microsoft.AspNetCore.Mvc;

namespace ImageProcessor.Controllers
{
    [ApiController]
    public class FilterController : ControllerBase
    {
        private readonly FilterService _filterService;

        public FilterController(FilterService filterService)
        {
            _filterService = filterService;
        }

        [HttpPost("/api/filter")]
        public async Task<IActionResult> ApplyFilter([FromForm] IFormFile image, [FromForm] string filter)
        {
            if (image == null || image.Length == 0 || string.IsNullOrWhiteSpace(filter))
            {
                return BadRequest("An image and filter are required.");
            }

            try
            {
                await using var input = image.OpenReadStream();
                var (processedBytes, contentType) = await _filterService.ProcessImageAsync(input, filter);
                return File(processedBytes, contentType);
            }
            catch (ArgumentException argEx)
            {
                return BadRequest(argEx.Message);
            }
            catch (Exception ex)
            {
                return StatusCode(500, ex.Message);
            }
        }


    }
}
