const BusinessDetailsForm = ({ values,
  handleChange,
  setFieldValue,
  errors,
  touched,
  disabled
}) => (
  <div className="space-y-3">
    {/* <h3 className="text-xl font-semibold">Business Details</h3> */}

    <div>
      <label>Business Name:</label>
      <input
        type="text"
        name="businessDetails.businessName"
        value={values.businessDetails?.businessName || ""}
        onChange={e => setFieldValue("businessDetails.businessName", e.target.value)}
        className="border p-2 w-full rounded"
        disabled={disabled}
      />
      {touched.businessDetails?.businessName && errors.businessDetails?.businessName && (
        <div className="text-red-500 text-sm">{errors.businessDetails.businessName}</div>
      )}
    </div>

    <div>
      <label>Business Email:</label>
      <input
        type="text"
        name="businessDetails.businessEmail"
        value={values.businessDetails?.businessEmail || ""}
        onChange={e => setFieldValue("businessDetails.businessEmail", e.target.value)}
        className="border p-2 w-full rounded"
        disabled={disabled}
      />
      {touched.businessDetails?.businessEmail && errors.businessDetails?.businessEmail && (
        <div className="text-red-500 text-sm">{errors.businessDetails.businessEmail}</div>
      )}
    </div>

    <div>
      <label>Business Phone:</label>
      <input
        type="text"
        name="businessDetails.businessPhone"
        value={values.businessDetails?.businessPhone || ""}
        onChange={e => setFieldValue("businessDetails.businessPhone", e.target.value)}
        className="border p-2 w-full rounded"
        disabled={disabled}
      />
      {touched.businessDetails?.businessPhone && errors.businessDetails?.businessPhone && (
        <div className="text-red-500 text-sm">{errors.businessDetails.businessPhone}</div>
      )}
    </div>

    {/* Add more fields as needed in the same pattern */}
  </div>
);

export default BusinessDetailsForm;
