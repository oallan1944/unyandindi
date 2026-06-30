import React from "react";



const BasicInfoForm = ({
    values,
    handleChange,
    setFieldValue,
    errors,
    touched,
    disabled
}) => (
    <div className="space-y-3">
        {/* <h3 className="text-xl font-semibold">Basic Information</h3> */}
        <div>
            <label>Name:</label>
            <input
                name="sellerName"
                value={values.sellerName}
                onChange={e => setFieldValue("sellerName", e.target.value)}
                className="border p-2 w-full rounded"
                disabled={disabled}
            />
            {touched.sellerName && errors.sellerName && (
                <div className="text-red-500 text-sm">{errors.sellerName}</div>
            )}
        </div>

        <div>
            <label>Email:</label>
            <input
                type="text"
                name="email"
                value={values.email}
                onChange={e => setFieldValue("email", e.target.value)}
                className="border p-2 w-full rounded"
                disabled={disabled}
            />
            {touched.email && errors.email && (
                <div className="text-red-500 text-sm">{errors.email}</div>
            )}
        </div>

        <div>
            <label>Mobile:</label>
            <input
                type="text"
                name="mobile"
                value={values.mobile}
                onChange={e => setFieldValue("mobile", e.target.value)}
                className="border p-2 w-full rounded"
                disabled={disabled}
            />
            {touched.mobile && errors.mobile && (
                <div className="text-red-500 text-sm">{errors.mobile}</div>
            )}
        </div>
        <div>
            <label>GSTIN:</label>
            <input
                type="text"
                name="gstin"
                value={values.gstin}
                onChange={e => setFieldValue("gstin", e.target.value)}
                className="border p-2 w-full rounded"
                disabled={disabled}
            />
            {touched.gstin && errors.gstin && (
                <div className="text-red-500 text-sm">{errors.gstin}</div>
            )}
        </div>
        {/* Repeat for email, mobile, GSTIN */}
    </div>
);

export default BasicInfoForm;
