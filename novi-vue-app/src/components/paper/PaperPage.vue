<template>
  <div class="paper-page shadow-sm">
    <!-- Header/Title: Only show if provided -->
    <div v-if="title" class="page-header mb-4 border-bottom pb-2">
      <h4 class="mb-0 fw-bold">{{ title }}</h4>
      <div v-if="subtitle" class="text-muted small mt-1" v-html="subtitle"></div>
    </div>

    <!-- Content Area -->
    <div class="page-content">
      <slot></slot>
    </div>

    <!-- Footer (Page Number) -->
    <div v-if="pageNumber" class="page-footer text-center text-muted small mt-4 pt-2 border-top">
      - {{ pageNumber }} -
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  },
  subtitle: {
    type: String,
    default: ''
  },
  pageNumber: {
    type: [Number, String],
    default: null
  }
})
</script>

<style scoped>
.paper-page {
  background: white;
  margin: 0 auto 30px; /* Separator */
  padding: 20mm; /* A4 padding */
  position: relative;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

/* Desktop: A4 Size (approx) or Responsive */
@media (min-width: 992px) {
  .paper-page {
    width: 100%;
    max-width: 210mm;
    min-height: 297mm;
  }
}

/* Mobile: Fluid */
@media (max-width: 992px) {
  .paper-page {
    width: 100%;
    min-height: auto;
    padding: 1.5rem;
    margin-bottom: 1rem;
    border-radius: 8px;
  }

  .page-footer {
    display: none;
  }
}

/* Print */
@media print {
  .paper-page {
    margin: 0;
    box-shadow: none;
    page-break-after: always;
    width: 100%;
  }
}
</style>
