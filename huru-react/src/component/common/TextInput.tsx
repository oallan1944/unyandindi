import React from "react";

interface TextInputProps {
    label: string;
    name: string;
    value: string;
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    error?: string;
    touched?: boolean;
    type?: string;
    className?: string;
}

const TextInput: React.FC<TextInputProps> = ({
    label,
    name,
    value,
    onChange,
    error,
    touched,
    type = "text",
    className = "border p-2 w-full rounded",
}) => {
    return (
        <div>
            <label>{label}</label>
            <input
                type={type}
                name={name}
                value={value}
                onChange={onChange}
                className={className}
            />
            {touched && error && (
                <div className="text-red-500 text-sm">{error}</div>
            )}
        </div>
    );
};

export default TextInput;
