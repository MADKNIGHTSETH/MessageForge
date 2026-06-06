<script setup>
import { ref, computed } from "vue";
import { useMessageStore } from "../../stores/message";
import {
  Mail,
  Phone,
  MessageCircle,
  MessageSquare,
  Globe,
  Link,
  X,
  Layers,
} from "@lucide/vue";

const messageStore = useMessageStore();
const selectedFilter = ref("All");

const channelTypes = [
  { label: "Tous", value: "All", icon: Layers },
  { label: "Email", value: "Email", icon: Mail },
  { label: "SMS", value: "SMS", icon: Phone },
  { label: "WhatsApp", value: "WhatsApp", icon: Globe },
  { label: "LinkedIn", value: "LinkedIn", icon: Link },
  { label: "Slack", value: "Slack", icon: MessageSquare },
  { label: "X", value: "X", icon: X },
  { label: "Messenger", value: "Messenger", icon: MessageCircle },
];

const history = computed(() => {
  if (selectedFilter.value === "All") {
    return messageStore.history;
  }
  return messageStore.history.filter((item) =>
    item.activeChannels.includes(selectedFilter.value),
  );
});
</script>

<template>
  <section class="space-y-6">
    <div
      class="rounded-3xl border border-sky-200 bg-sky-100 p-6 shadow-lg shadow-blue-900/10"
    >
      <!-- Filter Bar -->
      <div class="mt-6 flex flex-wrap gap-2">
        <button
          v-for="type in channelTypes"
          :key="type.value"
          @click="selectedFilter = type.value"
          :class="[
            'inline-flex items-center gap-2 rounded-full border px-4 py-2 text-xs font-medium transition',
            selectedFilter === type.value
              ? 'border-sky-600 bg-sky-600 text-white'
              : 'border-sky-200 bg-white text-slate-600 hover:border-sky-400',
          ]"
        >
          <component :is="type.icon" class="h-3.5 w-3.5" />
          {{ type.label }}
        </button>
      </div>
    </div>

    <div class="space-y-4">
      <div
        v-if="history.length === 0"
        class="rounded-3xl border border-sky-200 bg-sky-100 p-6 text-sm text-slate-600 text-center"
      >
        Aucun message trouvé pour ce filtre.
      </div>

      <div
        v-for="item in history"
        :key="item.id"
        class="rounded-3xl border border-sky-200 bg-sky-100 p-6 shadow-lg shadow-blue-900/10 transition hover:shadow-sky-200/50"
      >
        <div
          class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between"
        >
          <div>
            <div class="flex items-center gap-2">
              <span
                :class="[
                  'text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full',
                  item.status === 'SENT'
                    ? 'bg-emerald-100 text-emerald-700'
                    : 'bg-rose-100 text-rose-700',
                ]"
              >
                {{ item.status }}
              </span>
              <p class="text-[11px] text-slate-500">
                {{
                  item.sentAt
                    ? new Date(item.sentAt).toLocaleString("fr-FR")
                    : "Date inconnue"
                }}
              </p>
            </div>
            <h2 class="mt-2 text-lg font-semibold text-slate-950">
              {{ item.subject || "Sans objet" }}
            </h2>
          </div>
          <div v-if="selectedFilter === 'All'" class="flex flex-wrap gap-1.5">
            <span
              v-for="chan in item.activeChannels"
              :key="chan"
              class="rounded-lg border border-sky-200 bg-white px-2 py-1 text-[10px] font-medium text-sky-700"
            >
              {{ chan }}
            </span>
          </div>
        </div>

        <div
          class="mt-4 rounded-2xl border border-sky-50 bg-white/50 p-4 text-sm leading-6 text-slate-700"
        >
          <p class="text-slate-700">
            {{ item.rawContent || "Pas de contenu disponible" }}
          </p>
        </div>
      </div>
    </div>
  </section>
</template>
