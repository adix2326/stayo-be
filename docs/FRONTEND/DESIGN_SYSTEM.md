# Design System

StayO's frontend uses a unified design system powered by Tailwind CSS v4. It enforces consistency in colors, typography, spacing, and animations across the entire platform.

## Colors
The color palette is designed to be modern, trustworthy, and vibrant.

- **Primary**: `#1F2937` (Slate 800) - Used for primary text and heavy contrast elements.
- **Accent**: `#FC8019` (StayO Orange) - Used for primary actions, buttons, and highlights.
- **Secondary**: `#6B7280` (Gray 500) - Used for secondary text, borders, and disabled states.
- **Background (Light)**: `#F8FAFC` (Slate 50) - Default app background.
- **Background (Dark)**: `#0F172A` (Slate 900) - Used in Dark Mode.
- **Success**: `#22C55E` (Green)
- **Error**: `#EF4444` (Red)

## Typography
- **Primary Font**: `Poppins` (Google Fonts).
- The font scale uses standard Tailwind `text-sm`, `text-base`, `text-lg`, etc., mapped to the Poppins font family.

## Border Radius (Corners)
- `rounded-2xl` is standard for large interactive elements like primary buttons.
- `rounded-3xl` is used for floating cards and bottom sheets.
- `rounded-full` is used for tags, chips, and avatars.

## Shadows & Elevation
- Soft, highly diffused shadows are preferred over hard dropshadows.
- e.g. Primary Button Shadow: `shadow-[0_4px_12px_rgba(252,128,25,0.25)]`.

## Animations
StayO heavily utilizes micro-animations mapped in `tailwind.config.js`:
- `fade-in`: Standard opacity fade.
- `slide-up`: Modals and lists sliding into view.
- `sheet-up` / `sheet-down`: Bottom sheet physics.
- `chip-pop`: Springy entrance for tags.
- `heart-beat`: Wishlist interaction.
- Framer Motion is also used heavily on interactive elements (e.g., `<motion.button whileHover={{ scale: 1.008 }} whileTap={{ scale: 0.985 }}>`).

## Dark Mode
The application is pre-configured for dark mode via the `darkMode: 'class'` directive in Tailwind. Most common components (like `Input`) have `dark:text-slate-300`, `dark:bg-red-500/10` variants ready for full dark mode roll-out.
