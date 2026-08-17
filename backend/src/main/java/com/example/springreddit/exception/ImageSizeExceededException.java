package com.example.springreddit.exception;

/** Thrown before an oversized multipart file is copied into application memory. */
public class ImageSizeExceededException extends RuntimeException {

    public ImageSizeExceededException() {
        super("Dimensiunea imaginii nu poate depăși 5 MB");
    }
}
