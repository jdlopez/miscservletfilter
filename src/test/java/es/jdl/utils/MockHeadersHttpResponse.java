package es.jdl.utils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class MockHeadersHttpResponse implements HttpServletResponse {

    // just override HEADERS methods (only one header for each key)

    private HashMap<String, Object> headersContainer = new HashMap<>();

    @Override
    public void setDateHeader(String name, long date) {
        headersContainer.put(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        headersContainer.put(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        headersContainer.put(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        headersContainer.put(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        headersContainer.put(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        headersContainer.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        return headersContainer.containsKey(name)? headersContainer.get(name).toString():null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return Collections.singleton(getHeader(name));
    }

    @Override
    public Collection<String> getHeaderNames() {
        return headersContainer.keySet();
    }

    // END
    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override
    public String encodeUrl(String url) {
        return null;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return null;
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {

    }

    @Override
    public void sendError(int sc) throws IOException {

    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setStatus(int sc) {

    }

    @Override
    public void setStatus(int sc, String sm) {

    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return null;
    }

    @Override
    public void setCharacterEncoding(String charset) {

    }

    @Override
    public void setContentLength(int len) {

    }

    @Override
    public void setContentLengthLong(long len) {

    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
