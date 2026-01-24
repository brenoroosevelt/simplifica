<template>
  <div class="landing-page">
    <!-- Hero Section -->
    <LandingHero />

    <!-- Solutions Section -->
    <LandingSolutions />

    <!-- Benefits Section -->
    <LandingBenefits />

    <!-- Features Section -->
    <LandingFeatures />

    <!-- Use Cases Section -->
    <LandingUseCases />

    <!-- Social Proof Section -->
    <LandingSocialProof />

    <!-- Final CTA Section -->
    <LandingFinalCTA />
  </div>
</template>

<script setup lang="ts">
import LandingHero from '@/components/landing/LandingHero.vue'
import LandingSolutions from '@/components/landing/LandingSolutions.vue'
import LandingBenefits from '@/components/landing/LandingBenefits.vue'
import LandingFeatures from '@/components/landing/LandingFeatures.vue'
import LandingUseCases from '@/components/landing/LandingUseCases.vue'
import LandingSocialProof from '@/components/landing/LandingSocialProof.vue'
import LandingFinalCTA from '@/components/landing/LandingFinalCTA.vue'
</script>

<style scoped>
.landing-page {
  width: 100%;
  overflow-x: hidden;
}
</style>
