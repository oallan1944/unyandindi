import React from "react";

const BankDetailsForm = ({ values,
  handleChange,
  setFieldValue,
  errors,
  touched,
  disabled
}) => {
  return (
    <div className="space-y-3">
      {/* <h3 className="text-xl font-semibold">Bank Details</h3> */}
      <div>
        <label>Account Holder Name</label>
        <input
          type="text"
          name="bankDetails.accountHolderName"
          value={values.bankDetails?.accountHolderName || ""}
          onChange={e => setFieldValue("bankDetails.accountHolderName", e.target.value)}
          className="border p-2 w-full rounded"
          disabled={disabled}
        />
        {touched.bankDetails?.accountHolderName && errors.bankDetails?.accountHolderName && (
          <div className="text-red-500 text-sm">{errors.bankDetails.accountHolderName}</div>
        )}
      </div>

      <div>
        <label>Account Number</label>
        <input
          type="text"
          name="bankDetails.accountNumber"
          value={values.bankDetails?.accountNumber || ""}
          onChange={e => setFieldValue("bankDetails.accountNumber", e.target.value)}
          className="border p-2 w-full rounded"
          disabled={disabled}
        />
        {touched.bankDetails?.accountNumber && errors.bankDetails?.accountNumber && (
          <div className="text-red-500 text-sm">{errors.bankDetails.accountNumber}</div>
        )}
      </div>

      <div>
        <label>IFSC Code</label>
        <input
          type="text"
          name="bankDetails.ifscCode"
          value={values.bankDetails?.ifscCode || ""}
          onChange={e => setFieldValue("bankDetails.ifscCode", e.target.value)}
          className="border p-2 w-full rounded"
          disabled={disabled}
        />
        {touched.bankDetails?.ifscCode && errors.bankDetails?.ifscCode && (
          <div className="text-red-500 text-sm">{errors.bankDetails.ifscCode}</div>
        )}
      </div>
    </div>
  );
};

export default BankDetailsForm;
