import { useState, useCallback } from "react";
import { useInterval } from "./useInterval";

export const useCountdown = (initialTime: number = 30) => {
    const [timeLeft, setTimeLeft] = useState(0);

    const { start: startInterval, stop: stopInterval, isActive } = useInterval(
        () => {
            setTimeLeft((prev) => {
                if (prev <= 1) {
                    stopInterval();
                    return 0;
                }
                return prev - 1;
            });
        },
        1000,
        false
    );

    const start = useCallback(() => {
        setTimeLeft(initialTime);
        startInterval();
    }, [initialTime, startInterval]);

    const reset = useCallback(() => {
        setTimeLeft(0);
        stopInterval();
    }, [stopInterval]);

    return {
        timeLeft,
        isActive,
        start,
        reset,
    };
};
