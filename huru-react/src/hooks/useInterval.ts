import { useCallback, useEffect, useRef, useState } from "react";

export const useInterval = (callback: () => void, delay: number, autoStart = false) => {
    const [isActive, setIsActive] = useState(autoStart);
    const savedCallback = useRef(callback);
    const intervalId = useRef<NodeJS.Timeout | null>(null);

    // Remember latest callback
    useEffect(() => {
        savedCallback.current = callback;
    }, [callback]);

    // Set up interval
    useEffect(() => {
        if (isActive) {
            intervalId.current = setInterval(() => {
                savedCallback.current();
            }, delay);
        }
        return () => {
            if (intervalId.current) clearInterval(intervalId.current);
        };
    }, [isActive, delay]);

    const start = useCallback(() => {
        if (!isActive) setIsActive(true);
    }, [isActive]);

    const pause = useCallback(() => {
        if (intervalId.current) {
            clearInterval(intervalId.current);
            intervalId.current = null;
            setIsActive(false);
        }
    }, []);

    const stop = useCallback(() => {
        if (intervalId.current) {
            clearInterval(intervalId.current);
            intervalId.current = null;
        }
        setIsActive(false);
    }, []);

    return { start, pause, stop, isActive };
};
