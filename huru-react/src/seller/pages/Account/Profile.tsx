import { useEffect, useState } from 'react';
import { Formik, Form } from "formik";
import BasicInfoForm from "./BasicInfoForm";
import BusinessDetailsForm from "./BusinessDetailsForm";
import BankDetailsForm from "./BankDetailsForm";
import PickupAddressForm from "./PickupAddressForm";
import { profileValidationSchema } from "../../../component/validation/profileValidationSchema";
import { api } from "../../../config/Api";
import { SellerProfileFormValues } from "../../../types/sellerProfileFormValuesType";
import Card from "../../../component/common/Card"; // 👈 import the new Card component
import CardActions from "../../../component/common/CardActions";

const Profile = () => {
  const [initialValues, setInitialValues] = useState<SellerProfileFormValues | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [editingCard, setEditingCard] = useState<string | null>(null);

  const token = localStorage.getItem("jwt");

  const fetchProfile = async () => {
    try {
      const res = await api.get("/sellers/profile", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setInitialValues(res.data);
    } catch (err) {
      setError("Failed to load profile.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const handleSubmit = async (
    values: SellerProfileFormValues,
    { setSubmitting }: { setSubmitting: (isSubmitting: boolean) => void }
  ) => {
    try {
      await api.put(`/sellers/${values.id}`, values, {
        headers: { Authorization: `Bearer ${token}` },
      });
      alert("Profile updated successfully!");
    } catch (error) {
      alert("Failed to update profile.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <p>Loading profile...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className="max-w-4xl mx-auto mt-8 space-y-6">
      <h2 className="text-3xl font-bold mb-4">Edit Profile</h2>

      <Formik<SellerProfileFormValues>
        initialValues={initialValues!}
        validationSchema={profileValidationSchema}
        onSubmit={(values, actions) => handleSubmit(values, actions)}
        enableReinitialize
      >
        {({ isSubmitting, values, handleChange, setFieldValue, errors, touched }) => (
          <Form className="space-y-8">

            <Card title="Basic Information"
              actions={
                <CardActions
                  isEditing={editingCard === "basic"}
                  onEdit={() => setEditingCard("basic")}
                  onSave={() => setEditingCard(null)}
                />
              }
            >
              <BasicInfoForm
                values={values}
                handleChange={handleChange}
                errors={errors}
                touched={touched}
                setFieldValue={setFieldValue}
                disabled={editingCard !== "basic"}
              />
            </Card>

            <Card title="Business Details"
              actions={
                <CardActions
                  isEditing={editingCard === "business"}
                  onEdit={() => setEditingCard("business")}
                  onSave={() => setEditingCard(null)}
                />
              }
            >
              <BusinessDetailsForm
                values={values}
                handleChange={handleChange}
                errors={errors}
                setFieldValue={setFieldValue}
                touched={touched}
                disabled={editingCard !== "business"}
              />
            </Card>

            <Card title="Bank Details"
              actions={
                <CardActions
                  isEditing={editingCard === "bank"}
                  onEdit={() => setEditingCard("bank")}
                  onSave={() => setEditingCard(null)}
                />
              }
            >
              <BankDetailsForm
                values={values}
                setFieldValue={setFieldValue}
                handleChange={handleChange}
                errors={errors}
                touched={touched}
                disabled={editingCard !== "bank"}
              />
            </Card>

            <Card title="Pickup Address"
              actions={
                <CardActions
                  isEditing={editingCard === "pickup"}
                  onEdit={() => setEditingCard("pickup")}
                  onSave={() => setEditingCard(null)}
                />
              }
            >
              <PickupAddressForm
                setFieldValue={setFieldValue}
                values={values}
                handleChange={handleChange}
                errors={errors}
                touched={touched}
                disabled={editingCard !== "pickup"}
              />
            </Card>

            <button
              type="submit"
              disabled={isSubmitting}
              className={`w-full px-6 py-3 rounded text-white font-medium mt-2 transition 
                ${isSubmitting ? 'bg-blue-400 cursor-not-allowed' : 'bg-blue-600 hover:bg-blue-700'}`}
            >
              {isSubmitting ? "Updating..." : "Save All Changes"}
            </button>

          </Form>
        )}
      </Formik>
    </div>
  );
};

export default Profile;
