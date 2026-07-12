import i18next from "i18next";
import { initReactI18next } from "react-i18next";

import koCommon from "@/i18n/locales/ko/common.json";
import koAuth from "@/i18n/locales/ko/auth.json";
import koServiceRequest from "@/i18n/locales/ko/service-request.json";
import koIncident from "@/i18n/locales/ko/incident.json";
import koProblem from "@/i18n/locales/ko/problem.json";
import koChange from "@/i18n/locales/ko/change.json";
import koKnowledge from "@/i18n/locales/ko/knowledge.json";
import koAsset from "@/i18n/locales/ko/asset.json";
import koEsm from "@/i18n/locales/ko/esm.json";
import koVulnerability from "@/i18n/locales/ko/vulnerability.json";
import koCompliance from "@/i18n/locales/ko/compliance.json";
import koInfraMonitoring from "@/i18n/locales/ko/infra-monitoring.json";

import enCommon from "@/i18n/locales/en/common.json";
import enAuth from "@/i18n/locales/en/auth.json";
import enServiceRequest from "@/i18n/locales/en/service-request.json";
import enIncident from "@/i18n/locales/en/incident.json";
import enProblem from "@/i18n/locales/en/problem.json";
import enChange from "@/i18n/locales/en/change.json";
import enKnowledge from "@/i18n/locales/en/knowledge.json";
import enAsset from "@/i18n/locales/en/asset.json";
import enEsm from "@/i18n/locales/en/esm.json";
import enVulnerability from "@/i18n/locales/en/vulnerability.json";
import enCompliance from "@/i18n/locales/en/compliance.json";
import enInfraMonitoring from "@/i18n/locales/en/infra-monitoring.json";

import { readStoredLanguage } from "@/i18n/language";

/**
 * i18next 코어 초기화 — common.md 6.2/6.3절.
 * default 인스턴스 사용(Provider 불필요), main.tsx에서 사이드이펙트로 import한다.
 */
export const NAMESPACES = [
  "common",
  "auth",
  "service-request",
  "incident",
  "problem",
  "change",
  "knowledge",
  "asset",
  "esm",
  "vulnerability",
  "compliance",
  "infra-monitoring",
] as const;

i18next.use(initReactI18next).init({
  resources: {
    ko: {
      common: koCommon,
      auth: koAuth,
      "service-request": koServiceRequest,
      incident: koIncident,
      problem: koProblem,
      change: koChange,
      knowledge: koKnowledge,
      asset: koAsset,
      esm: koEsm,
      vulnerability: koVulnerability,
      compliance: koCompliance,
      "infra-monitoring": koInfraMonitoring,
    },
    en: {
      common: enCommon,
      auth: enAuth,
      "service-request": enServiceRequest,
      incident: enIncident,
      problem: enProblem,
      change: enChange,
      knowledge: enKnowledge,
      asset: enAsset,
      esm: enEsm,
      vulnerability: enVulnerability,
      compliance: enCompliance,
      "infra-monitoring": enInfraMonitoring,
    },
  },
  lng: readStoredLanguage(),
  fallbackLng: "ko",
  defaultNS: "common",
  ns: NAMESPACES,
  interpolation: {
    escapeValue: false,
  },
});

export default i18next;
