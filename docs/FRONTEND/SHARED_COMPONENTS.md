# Shared Components

Shared components reside in `src/components/common/` and are highly reusable, stateless (mostly), and styled entirely using the Design System.

## Button (`Button.jsx`)
A highly reusable Framer Motion powered button.

### Props
- `variant` (String): `primary` | `secondary` | `outline` | `ghost`. (Default: `primary`).
- `isLoading` (Boolean): If true, disables the button and shows a `Loader2` spinner.
- `fullWidth` (Boolean): If true, applies `w-full`. (Default: `true`).
- `children`: The button content.
- `onClick`: Click handler.

### Performance Notes
Uses Framer Motion for `whileHover` and `whileTap` scaling. Avoids heavy re-renders.

## Input (`Input.jsx`)
A controlled text input field with built-in labeling and error handling. Wrapped in `forwardRef` to support React Hook Form (if implemented later).

### Props
- `label` (String): The input label.
- `id` (String): HTML ID for accessibility.
- `type` (String): Input type (text, password, email).
- `error` (String): Error message to display below the input. Highlights border in red.
- `icon` (Lucide React Component): Renders an icon inside the input on the left.

## Bottom Navigation (`BottomNav.jsx`)
The primary mobile navigation bar. Conditionally renders active states based on current route.
- Links to: `/dashboard`, `/search`, `/my-bookings`, `/wishlist`, `/profile`.

## PWA Install Banner (`PWAInstallBanner.jsx`)
A global component that listens for the `beforeinstallprompt` event. If the app is not installed (and the user hasn't dismissed it), it prompts the user to "Add StayO to Home Screen" for a native app experience.
