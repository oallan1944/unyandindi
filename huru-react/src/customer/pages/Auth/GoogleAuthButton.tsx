import { Button } from '@mui/material';
import GoogleIcon from '@mui/icons-material/Google';
import { API_URL } from '../../../config/Api';

/**
 * Redirects the browser to the backend's Google OAuth2 authorization
 * endpoint (SecurityConfig: oauth2Login -> authorizationEndpoint baseUri
 * "/api/auth/google").
 *
 * This MUST be a full browser navigation (window.location), not an
 * axios/fetch call through the `api` instance. The OAuth2 redirect flow
 * requires the browser itself to follow the chain: this app -> backend
 * -> accounts.google.com -> backend callback -> back to this app. An
 * XHR request cannot participate in that redirect chain or receive
 * Google's consent-screen response.
 */
const GoogleAuthButton = () => {
    const handleGoogleLogin = () => {
        window.location.href = `${API_URL}/api/auth/google`;
    };

    return (
        <Button
            onClick={handleGoogleLogin}
            fullWidth
            variant="outlined"
            startIcon={<GoogleIcon />}
            sx={{ py: '11px', textTransform: 'none' }}
        >
            Continue with Google
        </Button>
    );
};

export default GoogleAuthButton;
