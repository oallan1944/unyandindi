import React, { useState } from "react";
import {
  Dialog, DialogTitle, DialogContent, IconButton,
  Grid, TextField, Button, CircularProgress,
  FormControl, InputLabel, Select, MenuItem
} from "@mui/material";
import { Close, AddPhotoAlternate } from "@mui/icons-material";
import { useFormik } from "formik";
import { uploadToCloudnary } from "../../../Util/UploadToCloudnary";
import { HomeCategory } from "../../../types/HomeCategoryType";
import { useAppDispatch } from "../../../State/store";
import { createHomeCategory, updateHomeCategory } from "../../../State/admin/AdminSlice";

type HomeSection = "GRID" | "SHOP_BY_CATEGORIES" | "ELECTRIC_CATEGORIES" | "DEALS";

interface Props {
  open: boolean;
  handleClose: () => void;
  section: HomeSection;
  category?: HomeCategory | null;   // present = edit mode, absent = create mode
}

function AddHomeCategory({ open, handleClose, section, category }: Props) {
  const dispatch = useAppDispatch();
  const [uploadingImage, setUploadingImage] = useState(false);
  const isEditMode = Boolean(category?.id);

  const formik = useFormik({
    enableReinitialize: true,
    initialValues: {
      name: category?.name ?? "",
      categoryId: category?.categoryId ?? "",
      image: category?.image ?? "",
      section: category?.section ?? section,
    },

    validate: (values) => {
      const errors: Record<string, string> = {};
      if (!values.name.trim()) errors.name = "Name is required";
      if (!values.categoryId.trim()) errors.categoryId = "Category ID is required";
      else if (!/^[a-z0-9_]+$/.test(values.categoryId)) {
        errors.categoryId = "Use lowercase letters, numbers, and underscores only";
      }
      if (!values.image) errors.image = "An image is required";
      return errors;
    },

    onSubmit: async (values, { resetForm }) => {
      const payload = { ...values } as HomeCategory;
      const result = isEditMode && category?.id
        ? await dispatch(updateHomeCategory({ id: category.id, data: payload }))
        : await dispatch(createHomeCategory(payload));

      const succeeded = isEditMode
        ? updateHomeCategory.fulfilled.match(result)
        : createHomeCategory.fulfilled.match(result);

      if (succeeded) {
        resetForm();
        handleClose();
      }
    }
  });

  const handleImageChange = async (event: any) => {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      formik.setFieldError("image", "File must be an image");
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      formik.setFieldError("image", "Image must be under 5MB");
      return;
    }

    setUploadingImage(true);
    try {
      const imageUrl = await uploadToCloudnary(file);
      if (!imageUrl) throw new Error("Upload returned no URL");
      formik.setFieldValue("image", imageUrl);
    } catch (error) {
      console.error("Upload failed", error);
      formik.setFieldError("image", "Upload failed — try again");
    } finally {
      setUploadingImage(false);
    }
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        {isEditMode ? "Edit" : "Add"} {section.replace(/_/g, " ").toLowerCase()} tile
        <IconButton onClick={handleClose} sx={{ position: "absolute", right: 10, top: 10 }}>
          <Close />
        </IconButton>
      </DialogTitle>

      <DialogContent>
        <form onSubmit={formik.handleSubmit}>
          <Grid container spacing={2} mt={1}>
            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Category Name"
                name="name"
                value={formik.values.name}
                onChange={formik.handleChange}
                error={!!formik.errors.name}
                helperText={formik.errors.name}
              />
            </Grid>

            <Grid size={{ xs: 12, md: 6 }}>
              <TextField
                fullWidth
                label="Category ID"
                name="categoryId"
                placeholder="e.g. gaming_laptop"
                value={formik.values.categoryId}
                onChange={formik.handleChange}
                disabled={isEditMode}
                error={!!formik.errors.categoryId}
                helperText={formik.errors.categoryId || "Must match a real product Category ID to power \"Buy now\""}
              />
            </Grid>

            {!category && (
              <Grid size={{ xs: 12, md: 6 }}>
                <FormControl fullWidth>
                  <InputLabel id="section-label">Section</InputLabel>
                  <Select
                    labelId="section-label"
                    name="section"
                    value={formik.values.section}
                    label="Section"
                    onChange={formik.handleChange}
                  >
                    <MenuItem value="GRID">Grid</MenuItem>
                    <MenuItem value="SHOP_BY_CATEGORIES">Shop By Category</MenuItem>
                    <MenuItem value="ELECTRIC_CATEGORIES">Electric Category</MenuItem>
                    <MenuItem value="DEALS">Deals</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
            )}

            <Grid size={{ xs: 12, md: 6 }}>
              <input type="file" hidden id="image-upload" onChange={handleImageChange} accept="image/*" />
              <label htmlFor="image-upload">
                <Button variant="outlined" component="span" startIcon={<AddPhotoAlternate />}>
                  {formik.values.image ? "Replace Image" : "Upload Image"}
                </Button>
              </label>
              {uploadingImage && <CircularProgress size={24} sx={{ ml: 2 }} />}
              {formik.errors.image && (
                <p className="text-red-500 text-sm mt-1">{formik.errors.image}</p>
              )}
            </Grid>

            {formik.values.image && (
              <Grid size={{ xs: 12 }}>
                <img src={formik.values.image} alt="preview" style={{ width: "120px", borderRadius: "8px" }} />
              </Grid>
            )}

            <Grid size={{ xs: 12 }}>
              <Button
                fullWidth
                variant="contained"
                type="submit"
                disabled={uploadingImage || formik.isSubmitting}
              >
                {isEditMode ? "Save Changes" : "Create"}
              </Button>
            </Grid>
          </Grid>
        </form>
      </DialogContent>
    </Dialog>
  );
}

export default AddHomeCategory;