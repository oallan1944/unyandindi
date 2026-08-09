import { Button, Step, StepLabel, Stepper } from '@mui/material'
import { useState } from 'react';
import BecomeSellerFormStep1 from './BecomeSellerFormStep1'
import { useFormik } from 'formik'
import BecomeSellerFormStep2 from './BecomeSellerFormStep2'
import BecomeSellerFormStep3 from './BecomeSellerFormStep3'
import BecomeSellerFormStep4 from './BecomeSellerFormStep4'
import { createSeller, useAppDispatch } from '../../../State/seller/sellerSlice'
import { SellerProfileFormValues } from '../../../types/sellerProfileFormValuesType'

const steps = [
    "Tax Details & Mobile",
    "Pickup Address",
    "Bank Details",
    "Supplier Details"
]

const SellerAccountForm = () => {
    const [activeStep, setActiveStep] = useState(0)
    const dispatch = useAppDispatch()
 
    const formik = useFormik<SellerProfileFormValues>({
        initialValues: {
            mobile: "",

            GSTIN: "",

            pickupAddress: {
                street: "",
                city: "",
                state: "",
                postalCode: "",
                country: "",
            },
            
            bankDetails: {
                bankName: "",
                accountNumber: "",
                ifscCode: "",
                branch: "",
            },
            sellerName: "",
            email: "",
            businessDetails: {
                businessName: "",
                businessType: "",
                businessPhone: "",
                businessAddress: ""
            },
            password: ""
        },
        //validation schema
        onSubmit: async (values) => {
            try {
                console.log("Submitting seller account", values);
                const res = await dispatch(createSeller(values)).unwrap();
                console.log("Seller registered", res)
                // calling dispatch to create seller
                // await dispatch (createSeller)
                alert("Seller Account created successfully!")
            } catch (err) {
                console.error("Failed to create seller", err)
                alert("Failed to create seller account.")
            }
        }
    })

    // Move between steps or submit on final step
    const handleNext = () => {
        if (activeStep === steps.length - 1) {
            formik.handleSubmit()
        } else {
            setActiveStep(prev => prev + 1)
        }
    }

    const handleBack = () => {
        if (activeStep > 0) {
            setActiveStep(prev => prev - 1)
        }
    }

    const renderStepContent = (step: number) => {
        switch (step) {
            case 0:
                return <BecomeSellerFormStep1 formik={formik} />
            case 1:
                return <BecomeSellerFormStep2 formik={formik} />
            case 2:
                return <BecomeSellerFormStep3 formik={formik} />
            case 3:
                return <BecomeSellerFormStep4 formik={formik} />
            default:
                return null
        }
    }
    return (
        <div>
            <Stepper activeStep={activeStep} alternativeLabel>
                {steps.map((Label, index) => (
                    <Step key={Label}>
                        <StepLabel>{Label}</StepLabel>
                    </Step>
                ))}
            </Stepper>
            <section className='mt-20 space-y-10' >
                <div>
                    {renderStepContent(activeStep)}
                </div>
                {/* <div>
                    {activeStep === 0 ? (<BecomeSellerFormStep1 formik={formik} />)
                        : activeStep === 1 ? (<BecomeSellerFormStep2 formik={formik} />)
                            : activeStep === 2 ? (<BecomeSellerFormStep3 formik={formik} />)
                                : (<BecomeSellerFormStep4 formik={formik} />)}
                </div> */}
                <div className='flex items-center justify-between'>
                    <Button onClick={handleBack} variant='contained' disabled={activeStep === 0}>
                        Back
                    </Button>
                    <Button onClick={handleNext} variant='contained' >
                        {activeStep === steps.length - 1 ? "Create Account" : "Continue"}
                    </Button>
                </div>
            </section>

        </div>
    )
}

export default SellerAccountForm

