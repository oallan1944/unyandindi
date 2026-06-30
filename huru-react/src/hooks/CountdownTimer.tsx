import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";

interface CountdownTimerProps {
    targetTime: Date;
    label?: string;
    className?: string;
}

const CountdownTimer: React.FC<CountdownTimerProps> = ({ targetTime, label, className }) => {
    const calculateTimeLeft = () => {
        const difference = +targetTime - +new Date();
        let timeLeft = { days: 0, hours: 0, minutes: 0, seconds: 0 };

        if (difference > 0) {
            timeLeft = {
                days: Math.floor(difference / (1000 * 60 * 60 * 24)),
                hours: Math.floor((difference / (1000 * 60 * 60)) % 24),
                minutes: Math.floor((difference / (1000 * 60)) % 60),
                seconds: Math.floor((difference / 1000) % 60),
            };
        }
        return timeLeft;
    };

    const [timeLeft, setTimeLeft] = useState(calculateTimeLeft());

    useEffect(() => {
        const timer = setInterval(() => {
            setTimeLeft(calculateTimeLeft());
        }, 1000);
        return () => clearInterval(timer);
    }, [targetTime]);

    const isExpired = +targetTime - +new Date() <= 0;

    const formatNumber = (num: number) => String(num).padStart(2, "0");

    return (
        <div className={`flex flex-col items-center ${className}`}>
            {label && (
                <p className="text-sm font-medium text-brandWhite-color mb-1">{label}</p>
            )}

            {!isExpired ? (
                <motion.div
                    className="flex space-x-2"
                    initial={{ opacity: 0, y: 10 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.4 }}
                >
                    {["days", "hours", "minutes", "seconds"].map((unit) => (
                        <div key={unit} className="flex flex-col items-center">
                            <span className="text-lg font-bold text-white">
                                {formatNumber((timeLeft as any)[unit])}
                            </span>
                            <span className="text-xs text-white/70 capitalize">{unit}</span>
                        </div>
                    ))}
                </motion.div>
            ) : (
                <p className="text-sm text-red-500 font-semibold">Offer Ended</p>
            )}
        </div>
    );
};

export default CountdownTimer;
