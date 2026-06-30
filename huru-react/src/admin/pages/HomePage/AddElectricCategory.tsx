import React, { useState } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  IconButton,
  Grid,
  TextField,
  Button,
  CircularProgress
} from "@mui/material";

import { Close, AddPhotoAlternate } from "@mui/icons-material";
import { useFormik } from "formik";
import { uploadToCloudnary } from "../../../Util/UploadToCloudnary";
import { HomeCategory } from "../../../types/HomeCategoryType";

interface Props {
  open: boolean;
  handleClose: () => void;
  category: HomeCategory;
}

 function AddElectricCategory({
  open,
  handleClose,
  category
}: Props) {

  const [uploadingImage, setUploadingImage] = useState(false);

  const formik = useFormik({
    initialValues: {
      name: "",
      description: "",
      price: "",
      image: "",
      categoryId: category.categoryId
    },

    onSubmit: (values) => {
      console.log("Item values:", values);

      /**
       * Dispatch your redux / API call here
       *
       * Example:
       * dispatch(createElectricItem(values))
       */

      handleClose();
    }
  });

  const handleImageChange = async (event: any) => {
    const file = event.target.files[0];
    if (!file) return;

    setUploadingImage(true);

    try {
      const imageUrl = await uploadToCloudnary(file);
      formik.setFieldValue("image", imageUrl);
    } catch (error) {
      console.error("Upload failed", error);
    } finally {
      setUploadingImage(false);
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="lg" fullWidth>

      <DialogTitle>
        Add Item - {category.categoryId}

        <IconButton
          onClick={handleClose}
          sx={{ position: "absolute", right: 10, top: 10 }}
        >
          <Close />
        </IconButton>
      </DialogTitle>

      <DialogContent>

        <form onSubmit={formik.handleSubmit}>

          <Grid container spacing={2} mt={1}>

            <Grid size={{ xs: 12, md: 4, lg: 3 }}>
              <TextField
                fullWidth
                label="Item Name"
                name="name"
                value={formik.values.name}
                onChange={formik.handleChange}
              />
            </Grid>

            <Grid size={{ xs: 12, md: 4, lg: 3 }}>
              <TextField
                fullWidth
                label="Description"
                name="description"
                value={formik.values.description}
                onChange={formik.handleChange}
              />
            </Grid>

            <Grid size={{ xs: 12, md: 4, lg: 3 }}>
              <TextField
                fullWidth
                label="Price"
                name="price"
                type="number"
                value={formik.values.price}
                onChange={formik.handleChange}
              />
            </Grid>

            {/* IMAGE UPLOAD */}

            <Grid size={{ xs: 12, md: 4, lg: 3 }}>

              <input
                type="file"
                hidden
                id="image-upload"
                onChange={handleImageChange}
              />

              <label htmlFor="image-upload">
                <Button
                  variant="outlined"
                  component="span"
                  startIcon={<AddPhotoAlternate />}
                >
                  Upload Image
                </Button>
              </label>

              {uploadingImage && <CircularProgress size={24} />}

            </Grid>

            {formik.values.image && (
              <Grid size={{ xs: 12, md: 4, lg: 3 }}>
                <img
                  src={formik.values.image}
                  alt="preview"
                  style={{ width: "120px", borderRadius: "8px" }}
                />
              </Grid>
            )}

            <Grid size={{ xs: 12, md: 4, lg: 3 }}>
              <Button
                fullWidth
                variant="contained"
                type="submit"
              >
                Save
              </Button>
            </Grid>

          </Grid>

        </form>

      </DialogContent>
    </Dialog>
  );
}

export default AddElectricCategory;