const PickupAddressForm = ({ values,
  handleChange,
  setFieldValue,
  errors,
  touched,
  disabled
}) => (
  <div className="space-y-3">
    {/* <h3 className="text-xl font-semibold">Pickup Address</h3> */}
    <div>
      <label>Address:</label>
      <input
        type="text"
        name="pickupAddress.address"
        value={values.pickupAddress?.address || ""}
        onChange={e => setFieldValue("pickupAddress.address", e.target.value)}
        className="border p-2 w-full rounded"
        disabled={disabled}
      />
      {touched.pickupAddress?.address && errors.pickupAddress?.address && (
        <div className="text-red-500 text-sm">{errors.pickupAddress.address}</div>
      )}
    </div>

    <div>
      <label>City:</label>
      <input
        type="text"
        name="pickupAddress.city"
        value={values.pickupAddress?.city || ""}
        onChange={e => setFieldValue("pickupAddress.city", e.target.value)}
        className="border p-2 w-full rounded"
        disabled={disabled}
      />
      {touched.pickupAddress?.city && errors.pickupAddress?.city && (
        <div className="text-red-500 text-sm">{errors.pickupAddress.city}</div>
      )}
    </div>

    <div>
      <label>State:</label>
      <input
        type="text"
        name="pickupAddress.state"
        value={values.pickupAddress?.state || ""}
        onChange={e => setFieldValue("pickupAddress.state", e.target.value)}
        className="border p-2 w-full rounded"
        disabled={disabled}
      />
      {touched.pickupAddress?.state && errors.pickupAddress?.state && (
        <div className="text-red-500 text-sm">{errors.pickupAddress.state}</div>
      )}
    </div>

    <div>
      <label>Mobile:</label>
      <input
        type="text"
        name="pickupAddress.mobile"
        value={values.pickupAddress?.mobile || ""}
        onChange={e => setFieldValue("pickupAddress.mobile", e.target.value)}
        className="border p-2 w-full rounded"
        disabled={disabled}
      />
      {touched.pickupAddress?.mobile && errors.pickupAddress?.mobile && (
        <div className="text-red-500 text-sm">{errors.pickupAddress.mobile}</div>
      )}
    </div>

    <div>
      <label>Pin Code:</label>
      <input
        type="text"
        name="pickupAddress.pinCode"
        value={values.pickupAddress?.pinCode || ""}
        onChange={e => setFieldValue("pickupAddress.pinCode", e.target.value)}
        className="border p-2 w-full rounded"
        disabled={disabled}
      />
      {touched.pickupAddress?.pinCode && errors.pickupAddress?.pinCode && (
        <div className="text-red-500 text-sm">{errors.pickupAddress.pinCode}</div>
      )}
    </div>
  </div>
);

export default PickupAddressForm;
