import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/context/AuthContext";
import { ThemeProvider } from "@/context/ThemeContext";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
    title: "ProjectHub - Workspace",
    description: "Enterprise project management and reporting platform",
};

export default function RootLayout({
                                       children,
                                   }: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en" className="dark">
        <body className={`${inter.className} antialiased transition-colors duration-200`}>
        <ThemeProvider>
            <AuthProvider>{children}</AuthProvider>
        </ThemeProvider>
        </body>
        </html>
    );
}
