import { ReactNode } from 'react'
import {APPLICATION_NAME} from "@/constants/AppConstants";

type LayoutProps = {
  children: ReactNode
}

export default function Layout({ children }: LayoutProps) {
  return (
    <div className="flex min-h-screen flex-col bg-black">
      <header className="border-b p-4 h-16">
        <h1 className="text-3xl font-bold leading-normal tracking-normal text-white">{APPLICATION_NAME}</h1>
      </header>

      <main className="flex-1 p-4">{children}</main>
    </div>
  )
}
