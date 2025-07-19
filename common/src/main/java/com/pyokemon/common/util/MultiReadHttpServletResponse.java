package com.pyokemon.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;


public class MultiReadHttpServletResponse extends HttpServletResponseWrapper {

  private ByteArrayOutputStream byteArrayOutputStream;
  private ServletOutputStream servletOutputStream;
  private PrintWriter printWriter;
  private boolean outputStreamUsed = false;
  private boolean writerUsed = false;

  public MultiReadHttpServletResponse(HttpServletResponse response) {
    super(response);
    this.byteArrayOutputStream = new ByteArrayOutputStream();
  }



  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    if (writerUsed) {
      throw new IllegalStateException("Cannot use OutputStream after Writer has been used");
    }
    outputStreamUsed = true;
    if (servletOutputStream == null) {
      servletOutputStream =
          new MultiReadServletOutputStream(byteArrayOutputStream, super.getOutputStream());
    }
    return servletOutputStream;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    if (outputStreamUsed) {
      throw new IllegalStateException("Cannot use Writer after OutputStream has been used");
    }
    writerUsed = true;
    if (printWriter == null) {
      servletOutputStream =
          new MultiReadServletOutputStream(byteArrayOutputStream, super.getOutputStream());
      printWriter =
          new PrintWriter(new OutputStreamWriter(servletOutputStream, getCharacterEncoding()));
    }
    return printWriter;
  }

  @Override
  public void flushBuffer() throws IOException {
    if (printWriter != null) {
      printWriter.flush();
    }
    if (servletOutputStream != null) {
      servletOutputStream.flush();
    }
  }

  public byte[] getCopy() {
    return byteArrayOutputStream.toByteArray();
  }

  private static class MultiReadServletOutputStream extends ServletOutputStream {
    private ByteArrayOutputStream byteArrayOutputStream;
    private ServletOutputStream originalOutputStream;

    public MultiReadServletOutputStream(ByteArrayOutputStream byteArrayOutputStream,
        ServletOutputStream originalOutputStream) {
      this.byteArrayOutputStream = byteArrayOutputStream;
      this.originalOutputStream = originalOutputStream;
    }

    @Override
    public boolean isReady() {
      return originalOutputStream.isReady();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
      originalOutputStream.setWriteListener(writeListener);
    }

    @Override
    public void write(int b) throws IOException {
      byteArrayOutputStream.write(b);
      originalOutputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      byteArrayOutputStream.write(b);
      originalOutputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      byteArrayOutputStream.write(b, off, len);
      originalOutputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
      byteArrayOutputStream.flush();
      originalOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
      byteArrayOutputStream.close();
      originalOutputStream.close();
    }
  }
}
