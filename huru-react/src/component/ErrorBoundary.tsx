// src/components/ErrorBoundary.tsx
import React, { Component, ReactNode } from 'react'

interface Props {
    children: ReactNode
    fallback?: ReactNode
}

interface State {
    hasError: boolean
    error: Error | null
}

class ErrorBoundary extends Component<Props, State> {
    constructor(props: Props) {
        super(props)
        this.state = { hasError: false, error: null }
    }

    static getDerivedStateFromError(error: Error): State {
        return { hasError: true, error }
    }

    componentDidCatch(error: Error, info: React.ErrorInfo) {
        console.error("ErrorBoundary caught:", error, info)
        // plug in Sentry or similar here in production
    }

    render() {
        if (this.state.hasError) {
            return this.props.fallback || (
                <div className="flex flex-col items-center justify-center h-screen gap-4">
                    <p className="text-red-500 text-xl font-semibold">
                        Something went wrong.
                    </p>
                    <button
                        className="px-4 py-2 bg-blue-500 text-white rounded"
                        onClick={() => this.setState({ hasError: false, error: null })}
                    >
                        Try Again
                    </button>
                </div>
            )
        }
        return this.props.children
    }
}

export default ErrorBoundary