package uk.gov.companieshouse.insolvency.data.auth;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EricTokenAuthenticationFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @Test
    @DisplayName("OAUTH2 GET request passes filter")
    void doFilterInternal() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter();

        when(request.getHeader("ERIC-Identity")).thenReturn("SOME-IDENTITY");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("OAUTH2");
        when(request.getMethod()).thenReturn("GET");

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("KEY type GET request passes filter")
    void doFilterInternalKey() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter();

        when(request.getHeader("ERIC-Identity")).thenReturn("SOME-IDENTITY");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("KEY");
        when(request.getMethod()).thenReturn("GET");

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("KEY type GET request with internal app privileges passes filter")
    void doFilterInternalKeyAndInternalApp() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter();

        when(request.getHeader("ERIC-Identity")).thenReturn("SOME-IDENTITY");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("KEY");
        when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("internal-app");
        when(request.getMethod()).thenReturn("GET");

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("KEY type PUT request with internal app privileges passes filter")
    void doFilterInternalMethodPutTypeKeyAndInternalApp() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter();

        when(request.getHeader("ERIC-Identity")).thenReturn("SOME-IDENTITY");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("KEY");
        when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("internal-app");
        when(request.getMethod()).thenReturn("PUT");

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Request with no identity fails ")
    void doFilterInternalNoIdentity() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter();

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
    }

    @Test
    @DisplayName("Request with no identity type fails ")
    void doFilterInternalNoIdentityType() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter();

        when(request.getHeader("ERIC-Identity")).thenReturn("SOME-IDENTITY");

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
    }

    @Test
    @DisplayName("Request with wrong identity type fails ")
    void doFilterInternalWrongIdentityType() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter();

        when(request.getHeader("ERIC-Identity")).thenReturn("SOME-IDENTITY");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("identityType");

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
    }

    @Test
    @DisplayName("PUT Request with OAUTH2 type fails")
    void doFilterInternalOauth2WrongMethod() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter();

        when(request.getHeader("ERIC-Identity")).thenReturn("SOME-IDENTITY");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("OAUTH2");
        when(request.getMethod()).thenReturn("PUT");

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
    }

    @Test
    @DisplayName("PUT Request with OAUTH2 type and internal app privilege fails")
    void doFilterInternalOauth2WrongMethodWithPrivilege() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter();

        when(request.getHeader("ERIC-Identity")).thenReturn("SOME-IDENTITY");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("OAUTH2");
        when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("internal-app");
        when(request.getMethod()).thenReturn("PUT");

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
    }

    @Test
    @DisplayName("PUT Request with KEY type with no privileges fails")
    void doFilterInternalKeyNoPrivileges() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter();

        when(request.getHeader("ERIC-Identity")).thenReturn("SOME-IDENTITY");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("KEY");
        when(request.getMethod()).thenReturn("PUT");

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
    }

    @Test
    @DisplayName("PUT Request with KEY type with wrong privileges fails")
    void doFilterInternalKeyWrongPrivileges() throws ServletException, IOException {
        EricTokenAuthenticationFilter ericTokenAuthenticationFilter = new EricTokenAuthenticationFilter();

        when(request.getHeader("ERIC-Identity")).thenReturn("SOME-IDENTITY");
        when(request.getHeader("ERIC-Identity-Type")).thenReturn("KEY");
        when(request.getHeader("ERIC-Authorised-Key-Privileges")).thenReturn("privilege");
        when(request.getMethod()).thenReturn("PUT");

        ericTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(0)).doFilter(request, response);
    }
}