# BookStore ReactJS App

This is the UI where customers can buy books and Admin can maintain inventory, orders, users etc.

## Run the App in Local Machine
 Install the required dependencies
```
    yarn install
```

Start the application.
```
    yarn start
```

## Google Sign-In

Create a Google OAuth 2.0 Web application client in Google Cloud Console. Add
the frontend origin used by React (normally `http://localhost:3000`) to the
authorized JavaScript origins, then configure the same client ID for both
services before starting them:

Frontend `.env.local`:
```
REACT_APP_GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
```

Account service environment:
```
GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
```

The backend verifies the Google ID token and creates a local `STANDARD_USER`
account on first sign-in. Google passwords are never stored or handled by the
application.