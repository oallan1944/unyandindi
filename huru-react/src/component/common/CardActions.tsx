import React from "react";
import { Button, Stack } from "@mui/material";

type CardActionsProps = {
    isEditing: boolean;
    onEdit: () => void;
    onSave: () => void;
};

const CardActions: React.FC<CardActionsProps> = ({ isEditing, onEdit, onSave }) => {
    return (
        <div className="flex gap-2">
            <Stack direction="row" spacing={1}>
                {isEditing ? (
                    <Button variant="contained" size="small" onClick={onSave}>
                        Save
                    </Button>
                ) : (
                    <Button variant="outlined" size="small" onClick={onEdit}>
                        Edit
                    </Button>
                )}
            </Stack>
        </div>
    );
};

export default CardActions;
