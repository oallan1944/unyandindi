
import React from "react";

type CardProps = {
    title: string;
    children: React.ReactNode;
    actions?: React.ReactNode;
};

const ProfileCard: React.FC<CardProps> = ({ title, children, actions }) => {
    return (
        <div className="border rounded shadow p-6 mb-6">

            <div className="flex items-center justify-between mb-4">
                <h3 className="text-xl font-semibold">{title}</h3>
                {actions && <div className="space-x-2">{actions}</div>}
            </div>

            <div>{children}</div>
        </div>
    );
};

export default ProfileCard;
