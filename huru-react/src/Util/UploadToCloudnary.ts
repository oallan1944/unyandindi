
export const uploadToCloudnary = async (pics: any) => {
    const cloud_name = "dh7ndznea"
    const upload_preset = "allan-huru"

    if (pics) {
        const data = new FormData();
        data.append("file", pics);
        data.append("upload_preset", upload_preset);
        data.append("cloud_name", cloud_name);

        const res = await fetch(
            "https://api.cloudinary.com/v1_1/dh7ndznea/upload", {

            method: "POST",
            body: data,
        })
        const file = await res.json();
        return file.secure_url;
    }
    else {
        console.log("Pics not found");
        return null;
    }
}