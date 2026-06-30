import React from "react";
import { Button, ButtonProps } from "@mui/material";
import { useCountdown } from "../../hooks/useCountdown";


type ResendOtpButtonProps = {
  onResend: () => void;
  cooldown?: number;
  resendText?: string;
  resendPrefixText?: string;
  resendSuffixText?: string;
  buttonProps?: ButtonProps;
};

const ResendOtpButton: React.FC<ResendOtpButtonProps> = ({
  onResend,
  cooldown = 60,
  resendText = "Resend OTP",
  resendPrefixText = "Resend in",
  resendSuffixText = "s",
  buttonProps = {},
}) => {
  const { timeLeft, isActive, start } = useCountdown(cooldown);

  const handleClick = () => {
    onResend();
    start();
  };

  return (
    <Button
      onClick={handleClick}
      disabled={isActive}
      size="small"
      sx={{ textTransform: "none", fontSize: "0.85rem", ...buttonProps?.sx }}
      {...buttonProps}
    >
      {isActive ? `${resendPrefixText} ${timeLeft}${resendSuffixText}` : resendText}
    </Button>
  );
};

export default ResendOtpButton;
