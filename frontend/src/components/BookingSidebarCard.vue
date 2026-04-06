<script setup lang="ts">
defineProps<{
  priceLabel: string
  includedItems: Array<{ name: string; price: string }>
  optionalItems: Array<{ name: string; price: string }>
  caption: string
}>()
</script>

<template>
  <article class="booking-card panel panel-pad-lg panel-stack-lg">
    <div class="booking-card__header panel-copy">
      <p class="ui-eyebrow">Book your session</p>
      <h2 class="ui-title-price">{{ priceLabel }}</h2>
      <span class="ui-copy-meta">Base entry fee</span>
    </div>

    <div class="booking-card__section">
      <p class="ui-eyebrow">Included services</p>
      <div v-for="item in includedItems" :key="item.name" class="booking-card__line">
        <span>{{ item.name }}</span>
        <strong>{{ item.price }}</strong>
      </div>
    </div>

    <div class="booking-card__section">
      <p class="ui-eyebrow">Optional extras</p>
      <div
        v-for="item in optionalItems"
        :key="item.name"
        class="booking-card__line booking-card__line--muted"
      >
        <span>{{ item.name }}</span>
        <strong>{{ item.price }}</strong>
      </div>
    </div>

    <div class="booking-card__total">
      <span class="ui-copy-meta">Total from</span>
      <strong class="ui-title-price">{{ priceLabel }}</strong>
    </div>

    <button class="action-button booking-card__cta" type="button" disabled>Booking protected</button>

    <p class="booking-card__caption ui-copy-caption">{{ caption }}</p>
  </article>
</template>

<style scoped>
.booking-card {
  border-color: var(--line-strong);
}

.booking-card__section {
  display: grid;
  gap: var(--space-md);
}

.booking-card__line {
  display: flex;
  justify-content: space-between;
  gap: var(--space-md);
  padding: var(--space-md) var(--space-lg);
  border: 1px solid var(--line-soft);
  border-radius: var(--radius-control);
  background: var(--surface-glass);
}

.booking-card__line span {
  color: var(--text-body);
}

.booking-card__line strong {
  color: var(--accent-strong);
}

.booking-card__line--muted strong {
  color: var(--text-strong);
}

.booking-card__total {
  display: flex;
  justify-content: space-between;
  align-items: end;
  gap: var(--space-md);
  padding: var(--space-xl);
  border-radius: var(--radius-inner);
  background: var(--surface-glass);
}

.booking-card__cta {
  width: 100%;
}

@media (max-width: 680px) {
  .booking-card__line,
  .booking-card__total {
    flex-direction: column;
    align-items: start;
  }
}
</style>
