class ImageGallery {
  constructor(containerId) {
    this.container = document.getElementById(containerId);
    if (!this.container) return;
    this.mainImage = this.container.querySelector("[data-gallery-main]");
    this.thumbnails = Array.from(this.container.querySelectorAll("[data-gallery-thumb]"));
    this.currentIndex = 0;
    this.bind();
  }

  bind() {
    this.thumbnails.forEach((thumb, index) => {
      thumb.addEventListener("click", () => this.show(index));
    });

    let startX = null;
    if (this.mainImage) {
      this.mainImage.addEventListener("touchstart", (event) => {
        startX = event.changedTouches[0].clientX;
      });
      this.mainImage.addEventListener("touchend", (event) => {
        if (startX === null || this.thumbnails.length === 0) return;
        const endX = event.changedTouches[0].clientX;
        if (Math.abs(endX - startX) > 30) {
          if (endX < startX) this.show((this.currentIndex + 1) % this.thumbnails.length);
          else this.show((this.currentIndex - 1 + this.thumbnails.length) % this.thumbnails.length);
        }
        startX = null;
      });
    }
  }

  show(index) {
    this.currentIndex = index;
    const selected = this.thumbnails[index];
    if (!selected || !this.mainImage) return;
    this.mainImage.src = selected.dataset.full || selected.src;
    this.mainImage.alt = selected.alt;
    this.thumbnails.forEach((thumb, thumbIndex) => thumb.classList.toggle("active", thumbIndex === index));
  }
}

document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("[data-image-gallery]").forEach((element) => {
    if (element.id) new ImageGallery(element.id);
  });
});
