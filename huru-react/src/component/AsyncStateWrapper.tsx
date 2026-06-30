// src/components/AsyncStateWrapper.tsx
import React, { ReactNode } from 'react'
import { CircularProgress, Button } from '@mui/material'

interface Props {
    loading: boolean
    error: string | null
    empty: boolean
    onRetry?: () => void
    emptyMessage?: string
    children: ReactNode
}

const AsyncStateWrapper = ({
    loading,
    error,
    empty,
    onRetry,
    emptyMessage = "No data found.",
    children
}: Props) => {
    if (loading) return (
        <div className="flex justify-center items-center py-20">
            <CircularProgress />
        </div>
    )

    if (error) return (
        <div className="flex flex-col items-center py-20 gap-4">
            <p className="text-red-500">{error}</p>
            {onRetry && (
                <Button variant="contained" onClick={onRetry}>Retry</Button>
            )}
        </div>
    )

    if (empty) return (
        <div className="flex justify-center items-center py-20">
            <p className="text-gray-400">{emptyMessage}</p>
        </div>
    )

    return <>{children}</>
}

export default AsyncStateWrapper