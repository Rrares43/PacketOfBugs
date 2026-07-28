package io;

public class FormattedOutputWriter implements OutputWriter {
    private final OutputWriter outputWriter;

    public FormattedOutputWriter(OutputWriter outputWriter) {
        this.outputWriter = outputWriter;
    }

    @Override
    public void write(String message) {
        String formattedMessage = formatMessage(message);
        outputWriter.write(formattedMessage);
    }

    private String formatMessage(String message) {
        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("error") || lowerMessage.contains("invalid") || message.contains("not found") || lowerMessage.contains("failed") || lowerMessage.contains("doesn't")) {
            return TextFormatter.error("\n❌ " + message);
        } else if (lowerMessage.contains("success") || lowerMessage.contains("created") || message.contains("deleted")) {
            return TextFormatter.success("\n✓ " + message);
        } else if (lowerMessage.contains("warning") || lowerMessage.contains("are you sure") || lowerMessage.contains("already") || lowerMessage.contains("cancelled")) {
            return TextFormatter.warning("\n⚠ " + message);
        } else {
            return message;
        }
    }
}
