package com.personal.planner.infra.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CORS configuration in SecurityConfig.
 *
 * <p>Verifies that CORS is properly configured for frontend access:</p>
 * <ul>
 *   <li>Allowed origin: http://localhost:3000</li>
 *   <li>Allowed methods: GET, POST, PUT, DELETE, OPTIONS</li>
 *   <li>Allowed headers: Authorization, Content-Type</li>
 *   <li>Credentials enabled</li>
 * </ul>
 */
class CorsConfigTest {

    @Test
    @DisplayName("SecurityConfig should have corsConfigurationSource method")
    void securityConfigShouldHaveCorsMethod() throws NoSuchMethodException {
        Method corsMethod = SecurityConfig.class.getDeclaredMethod("corsConfigurationSource");
        assertNotNull(corsMethod);
        assertEquals(CorsConfigurationSource.class, corsMethod.getReturnType());
    }

    @Test
    @DisplayName("CORS configuration should allow localhost:3000 origin")
    void corsConfigShouldAllowLocalhost3000() throws Exception {
        // Create a SecurityConfig instance to test the CORS configuration
        // We need to use reflection since the constructor requires JwtAuthenticationFilter
        SecurityConfig config = createSecurityConfigWithMock();

        CorsConfigurationSource source = config.corsConfigurationSource();
        assertNotNull(source);
        assertTrue(source instanceof UrlBasedCorsConfigurationSource);

        // Get the configuration for /api/** path
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration corsConfig = urlSource.getCorsConfiguration(
            new MockHttpServletRequest("/api/test")
        );

        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowedOrigins().contains("http://localhost:3000"));
    }

    @Test
    @DisplayName("CORS configuration should allow required HTTP methods")
    void corsConfigShouldAllowRequiredMethods() throws Exception {
        SecurityConfig config = createSecurityConfigWithMock();
        CorsConfigurationSource source = config.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;

        CorsConfiguration corsConfig = urlSource.getCorsConfiguration(
            new MockHttpServletRequest("/api/test")
        );

        assertNotNull(corsConfig);
        List<String> allowedMethods = corsConfig.getAllowedMethods();
        assertTrue(allowedMethods.containsAll(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")));
    }

    @Test
    @DisplayName("CORS configuration should allow required headers")
    void corsConfigShouldAllowRequiredHeaders() throws Exception {
        SecurityConfig config = createSecurityConfigWithMock();
        CorsConfigurationSource source = config.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;

        CorsConfiguration corsConfig = urlSource.getCorsConfiguration(
            new MockHttpServletRequest("/api/test")
        );

        assertNotNull(corsConfig);
        List<String> allowedHeaders = corsConfig.getAllowedHeaders();
        assertTrue(allowedHeaders.contains("Authorization"));
        assertTrue(allowedHeaders.contains("Content-Type"));
    }

    @Test
    @DisplayName("CORS configuration should allow credentials")
    void corsConfigShouldAllowCredentials() throws Exception {
        SecurityConfig config = createSecurityConfigWithMock();
        CorsConfigurationSource source = config.corsConfigurationSource();
        UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;

        CorsConfiguration corsConfig = urlSource.getCorsConfiguration(
            new MockHttpServletRequest("/api/test")
        );

        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowCredentials());
    }

    private SecurityConfig createSecurityConfigWithMock() {
        // The corsConfigurationSource() method doesn't use jwtAuthFilter,
        // so we can pass null for testing purposes
        return new SecurityConfig(null);
    }

    /**
     * Simple mock HttpServletRequest for testing CORS configuration lookup.
     */
    private static class MockHttpServletRequest implements jakarta.servlet.http.HttpServletRequest {
        private final String requestUri;

        MockHttpServletRequest(String requestUri) {
            this.requestUri = requestUri;
        }

        @Override
        public String getRequestURI() {
            return requestUri;
        }

        @Override
        public String getServletPath() {
            return requestUri;
        }

        @Override
        public String getPathInfo() {
            return null;
        }

        // All other methods return null/default - not needed for CORS config lookup
        @Override public String getAuthType() { return null; }
        @Override public jakarta.servlet.http.Cookie[] getCookies() { return new jakarta.servlet.http.Cookie[0]; }
        @Override public long getDateHeader(String name) { return 0; }
        @Override public String getHeader(String name) { return null; }
        @Override public java.util.Enumeration<String> getHeaders(String name) { return java.util.Collections.emptyEnumeration(); }
        @Override public java.util.Enumeration<String> getHeaderNames() { return java.util.Collections.emptyEnumeration(); }
        @Override public int getIntHeader(String name) { return 0; }
        @Override public String getMethod() { return "GET"; }
        @Override public String getPathTranslated() { return null; }
        @Override public String getContextPath() { return ""; }
        @Override public String getQueryString() { return null; }
        @Override public String getRemoteUser() { return null; }
        @Override public boolean isUserInRole(String role) { return false; }
        @Override public java.security.Principal getUserPrincipal() { return null; }
        @Override public String getRequestedSessionId() { return null; }
        @Override public StringBuffer getRequestURL() { return new StringBuffer("http://localhost:8080" + requestUri); }
        @Override public jakarta.servlet.http.HttpSession getSession(boolean create) { return null; }
        @Override public jakarta.servlet.http.HttpSession getSession() { return null; }
        @Override public String changeSessionId() { return null; }
        @Override public boolean isRequestedSessionIdValid() { return false; }
        @Override public boolean isRequestedSessionIdFromCookie() { return false; }
        @Override public boolean isRequestedSessionIdFromURL() { return false; }
        @Override public boolean authenticate(jakarta.servlet.http.HttpServletResponse response) { return false; }
        @Override public void login(String username, String password) {}
        @Override public void logout() {}
        @Override public java.util.Collection<jakarta.servlet.http.Part> getParts() { return java.util.Collections.emptyList(); }
        @Override public jakarta.servlet.http.Part getPart(String name) { return null; }
        @Override public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
        @Override public Object getAttribute(String name) { return null; }
        @Override public java.util.Enumeration<String> getAttributeNames() { return java.util.Collections.emptyEnumeration(); }
        @Override public String getCharacterEncoding() { return null; }
        @Override public void setCharacterEncoding(String env) {}
        @Override public int getContentLength() { return 0; }
        @Override public long getContentLengthLong() { return 0; }
        @Override public String getContentType() { return null; }
        @Override public jakarta.servlet.ServletInputStream getInputStream() { return null; }
        @Override public String getParameter(String name) { return null; }
        @Override public java.util.Enumeration<String> getParameterNames() { return java.util.Collections.emptyEnumeration(); }
        @Override public String[] getParameterValues(String name) { return new String[0]; }
        @Override public java.util.Map<String, String[]> getParameterMap() { return java.util.Collections.emptyMap(); }
        @Override public String getProtocol() { return "HTTP/1.1"; }
        @Override public String getScheme() { return "http"; }
        @Override public String getServerName() { return "localhost"; }
        @Override public int getServerPort() { return 8080; }
        @Override public java.io.BufferedReader getReader() { return null; }
        @Override public String getRemoteAddr() { return "127.0.0.1"; }
        @Override public String getRemoteHost() { return "localhost"; }
        @Override public void setAttribute(String name, Object o) {}
        @Override public void removeAttribute(String name) {}
        @Override public java.util.Locale getLocale() { return java.util.Locale.getDefault(); }
        @Override public java.util.Enumeration<java.util.Locale> getLocales() { return java.util.Collections.emptyEnumeration(); }
        @Override public boolean isSecure() { return false; }
        @Override public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) { return null; }
        @Override public int getRemotePort() { return 0; }
        @Override public String getLocalName() { return "localhost"; }
        @Override public String getLocalAddr() { return "127.0.0.1"; }
        @Override public int getLocalPort() { return 8080; }
        @Override public jakarta.servlet.ServletContext getServletContext() { return null; }
        @Override public jakarta.servlet.AsyncContext startAsync() { return null; }
        @Override public jakarta.servlet.AsyncContext startAsync(jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) { return null; }
        @Override public boolean isAsyncStarted() { return false; }
        @Override public boolean isAsyncSupported() { return false; }
        @Override public jakarta.servlet.AsyncContext getAsyncContext() { return null; }
        @Override public jakarta.servlet.DispatcherType getDispatcherType() { return jakarta.servlet.DispatcherType.REQUEST; }
        @Override public String getRequestId() { return "test-request-id"; }
        @Override public String getProtocolRequestId() { return "test-protocol-request-id"; }
        @Override public jakarta.servlet.ServletConnection getServletConnection() { return null; }
    }
}
