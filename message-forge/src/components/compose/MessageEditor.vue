<script setup>
import { ref, computed, watch, onMounted } from "vue";
import { useMessageStore } from "../../stores/message";
import { usePreviewStore } from "../../stores/preview";
import { useAuthStore } from "../../stores/auth";
import {
  Mail,
  Phone,
  MessageCircle,
  MessageSquare,
  Globe,
  Link,
  X,
  Send,
  Sparkles,
  CheckCircle,
  ChevronDown,
} from "@lucide/vue";

const messageStore = useMessageStore();
const previewStore = usePreviewStore();
const authStore = useAuthStore();

const localText = ref(messageStore.currentDraft.rawContent);
const subjectText = ref(messageStore.currentDraft.subject);
const recipientText = ref(messageStore.currentDraft.recipient);
const activeChannels = computed(() => messageStore.currentDraft.activeChannels);
const isEmailActive = computed(() => activeChannels.value.includes("Email"));
const isSMSActive = computed(() => activeChannels.value.includes("SMS"));
const isLinkedInActive = computed(() =>
  activeChannels.value.includes("LinkedIn"),
);
const charCount = computed(() => localText.value.length);
const canSend = computed(
  () =>
    localText.value.trim().length > 0 &&
    activeChannels.value.length > 0 &&
    !messageStore.isSending,
);

const getAccountsFor = (type) => authStore.accountsByType(type);

const updateSelectedAccount = (channel, accountId) => {
  messageStore.updateDraft({
    selectedAccounts: {
      ...messageStore.currentDraft.selectedAccounts,
      [channel]: accountId,
    },
  });
};

const channelOptions = [
  { key: "Email", label: "Email", icon: Mail },
  { key: "SMS", label: "SMS", icon: Phone },
  { key: "LinkedIn", label: "LinkedIn", icon: Link },
  { key: "Telegram", label: "Telegram", icon: Globe },
  { key: "Slack", label: "Slack", icon: MessageSquare },
  { key: "X", label: "X", icon: X },
  { key: "Messenger", label: "Messenger", icon: MessageCircle },
  { key: "WhatsApp", label: "WhatsApp", icon: Globe },
];

const decoratorOptions = [
  { key: "emoji", label: "Emoji" },
  { key: "hashtag", label: "Hashtag" },
  { key: "signature", label: "Signature" },
];

const previewChannels = computed(() => activeChannels.value);

const updateDraftText = () => {
  messageStore.updateDraft({ rawContent: localText.value });
};

const updateSubjectText = () => {
  messageStore.updateDraft({ subject: subjectText.value });
};

const requestPreview = () => {
  previewStore.requestPreview(
    localText.value,
    previewChannels.value,
    messageStore.currentDraft.decorators,
  );
};

const toggleChannel = (channelKey) => {
  const current = activeChannels.value.includes(channelKey)
    ? activeChannels.value.filter((item) => item !== channelKey)
    : [...activeChannels.value, channelKey];

  messageStore.updateDraft({ activeChannels: current });
  requestPreview();
};

const toggleDecorator = (key) => {
  messageStore.updateDraft({
    decorators: {
      ...messageStore.currentDraft.decorators,
      [key]: !messageStore.currentDraft.decorators[key],
    },
  });
  requestPreview();
};

const sendMessage = async () => {
  await messageStore.sendCurrentDraft();
};

// Filter channels that have connected accounts
const channelsWithAccounts = computed(() => {
  return activeChannels.value.filter(ch => getAccountsFor(ch).length > 0);
});

watch(localText, () => {
  updateDraftText();
  requestPreview();
});

watch(subjectText, () => {
  updateSubjectText();
});

watch(recipientText, (val) => {
  messageStore.updateDraft({ recipient: val });
});

watch(
  () => messageStore.currentDraft.decorators,
  () => {
    requestPreview();
  },
  { deep: true },
);

watch(
  () => messageStore.currentDraft.id,
  () => {
    localText.value = messageStore.currentDraft.rawContent;
    subjectText.value = messageStore.currentDraft.subject;
    recipientText.value = messageStore.currentDraft.recipient;
    requestPreview();
  },
);

onMounted(() => {
  authStore.fetchConnectedAccounts().catch(() => null);
  requestPreview();
});
</script>

<template>
  <section class="grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
    <div
      class="space-y-6 rounded-3xl border border-sky-200 bg-sky-100 p-6 shadow-lg shadow-blue-900/10"
    >
      <div class="space-y-2">
        <h2 class="text-2xl font-semibold text-slate-950">
          Éditeur de message
        </h2>
        <p class="text-sm text-slate-500">
          Composez le contenu, activez les canaux et observez la
          prévisualisation instantanée.
        </p>
      </div>

      <div class="grid gap-3">
        <div class="space-y-3">
          <p class="text-sm font-medium text-slate-700">Canaux actifs</p>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="channel in channelOptions"
              :key="channel.key"
              type="button"
              @click="toggleChannel(channel.key)"
              :class="[
                'inline-flex items-center gap-2 rounded-full border px-4 py-2 text-sm transition focus:outline-none',
                activeChannels.includes(channel.key)
                  ? 'border-sky-600 bg-sky-200 text-sky-900'
                  : 'border-sky-200 bg-white text-slate-600 hover:border-sky-400 hover:bg-sky-50 hover:text-slate-950',
              ]"
            >
              <component :is="channel.icon" class="h-4 w-4" />
              {{ channel.label }}
            </button>
          </div>
        </div>

        <div
          v-if="channelsWithAccounts.length > 0"
          class="rounded-3xl border border-sky-100 bg-sky-50 p-4"
        >
          <p class="mb-3 text-sm font-medium text-slate-700">
            Sélection des comptes expéditeurs
          </p>
          <div class="grid gap-3 sm:grid-cols-2">
            <div v-for="channelKey in channelsWithAccounts" :key="channelKey" class="space-y-1.5">
              <label
                class="text-[11px] font-semibold uppercase tracking-wider text-slate-500"
                >Compte {{ channelKey }}</label
              >
              <div class="relative">
                <select
                  :value="messageStore.currentDraft.selectedAccounts[channelKey] || ''"
                  @change="
                    (e) => updateSelectedAccount(channelKey, e.target.value)
                  "
                  class="w-full appearance-none rounded-2xl border border-sky-200 bg-white px-4 py-2.5 text-sm text-slate-900 outline-none focus:border-sky-600"
                >
                  <option value="">Sélectionner un compte</option>
                  <option
                    v-for="acc in getAccountsFor(channelKey)"
                    :key="acc.id"
                    :value="acc.id"
                  >
                    {{ acc.label }} ({{ acc.value }})
                  </option>
                </select>
                <ChevronDown
                  class="pointer-events-none absolute right-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="space-y-3">
          <label class="text-sm font-medium text-slate-700"
            >Destinataire (Numéro, Email, ou Chat ID)</label
          >
          <input
            type="text"
            v-model="recipientText"
            placeholder="ex: +33612345678, test@mail.com, 123456789"
            class="w-full rounded-3xl border border-sky-200 bg-white px-4 py-3 text-slate-950 outline-none transition focus:border-sky-600"
          />
        </div>

        <div class="space-y-3">
          <label class="text-sm font-medium text-slate-700"
            >Objet du message</label
          >
          <input
            v-if="isEmailActive"
            type="text"
            v-model="subjectText"
            placeholder="Entrez l'objet du mail"
            class="w-full rounded-3xl border border-sky-200 bg-white px-4 py-3 text-slate-950 outline-none transition focus:border-sky-600"
          />
          <div
            v-else
            class="rounded-3xl border border-sky-200 bg-white px-4 py-3 text-slate-500"
          >
            L'objet est uniquement disponible pour Email.
          </div>
        </div>

        <div class="space-y-3 rounded-3xl border border-sky-100 bg-sky-50 p-4">
          <div class="flex items-center justify-between">
            <label class="text-sm font-medium text-slate-700">Texte brut</label>
            <span class="text-xs text-slate-500"
              >{{ charCount }} caractères</span
            >
          </div>
          <textarea
            v-model="localText"
            rows="12"
            placeholder="Saisissez votre message ici..."
            class="w-full resize-none rounded-3xl border border-sky-200 bg-white px-4 py-4 text-slate-950 outline-none transition focus:border-sky-600"
          />
        </div>

        <div class="rounded-3xl border border-sky-100 bg-sky-50 p-4">
          <p class="text-sm font-medium text-slate-700">Décorateurs</p>
          <div class="mt-3 grid gap-3 sm:grid-cols-3">
            <label
              v-for="decorator in decoratorOptions"
              :key="decorator.key"
              class="flex cursor-pointer items-center gap-3 rounded-3xl border border-sky-200 bg-white px-4 py-3 transition hover:border-sky-300"
            >
              <input
                type="checkbox"
                class="h-4 w-4 rounded border-sky-200 bg-sky-50 text-sky-700"
                :checked="messageStore.currentDraft.decorators[decorator.key]"
                @change="() => toggleDecorator(decorator.key)"
              />
              <span class="text-sm text-slate-700">{{ decorator.label }}</span>
            </label>
          </div>
        </div>

        <button
          type="button"
          @click="sendMessage"
          :disabled="!canSend"
          :class="[
            'inline-flex items-center justify-center gap-2 rounded-full px-6 py-3 text-sm font-semibold text-white transition',
            canSend ? 'bg-sky-600 hover:bg-sky-700' : 'cursor-not-allowed bg-slate-300',
          ]"
        >
          <Send class="h-4 w-4" />
          {{ messageStore.isSending ? "Envoi en cours..." : "Envoyer le message" }}
        </button>

        <div
          v-if="messageStore.error"
          class="rounded-2xl bg-rose-50 px-4 py-3 text-sm text-rose-700"
        >
          {{ messageStore.error }}
        </div>
      </div>
    </div>

    <aside class="space-y-6">
      <div
        class="rounded-3xl border border-sky-100 bg-sky-100 p-5 shadow-lg shadow-blue-900/10"
      >
        <div class="mb-4 flex items-center justify-between gap-3">
          <div>
            <p class="text-sm uppercase tracking-[0.25em] text-sky-700">
              Prévisualisation
            </p>
            <p class="text-xs text-slate-500">
              Résultat simulé par canal en temps réel.
            </p>
          </div>
          <span class="rounded-full bg-white px-3 py-1 text-xs text-slate-700"
            >{{ previewStore.previews.length }} canaux</span
          >
        </div>

        <div class="space-y-4">
          <div
            v-if="previewStore.isLoading"
            class="rounded-3xl bg-white p-4 text-sm text-slate-500"
          >
            Génération de la prévisualisation...
          </div>
          <div
            v-else-if="previewStore.error"
            class="rounded-3xl bg-rose-50 p-4 text-sm text-rose-700"
          >
            {{ previewStore.error }}
          </div>
          <div
            v-else-if="previewStore.previews.length === 0"
            class="rounded-3xl bg-white p-4 text-sm text-slate-500"
          >
            Activez des canaux et saisissez un message pour afficher la
            prévisualisation.
          </div>

          <div
            v-for="preview in previewStore.previews"
            :key="preview.channel"
            class="rounded-3xl border border-sky-100 bg-sky-50 p-4"
          >
            <div
              class="mb-3 flex items-center justify-between gap-3 text-sm font-semibold text-slate-950"
            >
              <span class="inline-flex items-center gap-2">
                <CheckCircle class="h-4 w-4 text-sky-700" />
                {{ preview.channel }}
              </span>
              <span class="text-xs text-slate-500"
                >{{ preview.charCount }} caractères</span
              >
            </div>
            <pre
              class="whitespace-pre-wrap break-words text-sm leading-6 text-slate-700"
              >{{ preview.formattedContent }}</pre
            >
            <div
              v-if="preview.warnings?.length"
              class="mt-3 space-y-1 text-sm text-sky-700"
            >
              <p v-for="warning in preview.warnings" :key="warning">
                • {{ warning }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div
        class="rounded-3xl border border-sky-100 bg-sky-100 p-5 shadow-lg shadow-blue-900/10"
      >
        <p class="text-sm uppercase tracking-[0.25em] text-slate-500">
          Statut d’envoi
        </p>
        <div
          v-if="messageStore.sendProgress.length === 0"
          class="mt-4 text-sm text-slate-500"
        >
          Appuyez sur Envoyer pour démarrer la progression par canal.
        </div>
        <div class="space-y-4" v-else>
          <div
            v-for="item in messageStore.sendProgress"
            :key="item.channel"
            class="space-y-2"
          >
            <div
              class="flex items-center justify-between text-sm text-slate-700"
            >
              <span>{{ item.channel }}</span>
              <span>{{ item.status }} · {{ item.progress }}%</span>
            </div>
            <div class="h-2 overflow-hidden rounded-full bg-white">
              <div
                :style="{ width: `${item.progress}%` }"
                class="h-full rounded-full bg-sky-600 transition-all duration-300"
              ></div>
            </div>
            <p v-if="item.errorMessage" class="text-sm text-rose-700">
              {{ item.errorMessage }}
            </p>
          </div>
        </div>
      </div>
    </aside>
  </section>
</template>
