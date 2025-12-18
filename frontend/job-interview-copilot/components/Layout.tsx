import { ReactNode } from 'react';

type LayoutProps = {
  children: ReactNode;
}

export default function Layout({ children }: LayoutProps) {
  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b p-4">
        <h1 className="text-lg font-semibold">
          Job & Interview Copilot
        </h1>
      </header>

      <main className="flex-1 p-4">
        {children}
      </main>
    </div>
  );
}
