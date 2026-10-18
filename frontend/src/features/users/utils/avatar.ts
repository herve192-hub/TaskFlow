/** Decode a bounded raster upload and save a small square PNG; SVG and animated images are excluded. */
export async function prepareAvatar(file: File): Promise<string> {
  if (!["image/png", "image/jpeg"].includes(file.type)) throw new Error("Choose a PNG or JPEG photo.");
  if (file.size > 2 * 1024 * 1024) throw new Error("Choose a photo smaller than 2 MB.");
  const bitmap = await createImageBitmap(file);
  try {
    if (bitmap.width * bitmap.height > 16_000_000) throw new Error("Choose a photo up to 16 megapixels.");
    const canvas = document.createElement("canvas");
    canvas.width = canvas.height = 256;
    const context = canvas.getContext("2d");
    if (!context) throw new Error("Unable to process this photo.");
    const side = Math.min(bitmap.width, bitmap.height);
    context.drawImage(bitmap, (bitmap.width - side) / 2, (bitmap.height - side) / 2, side, side, 0, 0, 256, 256);
    const image = canvas.toDataURL("image/png");
    if (image.length > 262144) throw new Error("This photo is too detailed. Try a simpler image.");
    return image;
  } finally { bitmap.close(); }
}
