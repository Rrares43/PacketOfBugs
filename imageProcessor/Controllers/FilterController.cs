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
        public async Task<IActionResult> ApplyFilter([FromBody] ImageProcessingRequest request)
        {
            
            if (request == null || string.IsNullOrEmpty(request.DownloadUrl) || string.IsNullOrEmpty(request.UploadUrl))
            {
                return BadRequest("Invalid request. DownloadUrl and UploadUrl are required.");
            }

            try
            {
                
                await _filterService.ProcessAndUploadImageAsync(request);

                return Ok(new { message = "Image processed and uploaded to S3 successfully." });
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
