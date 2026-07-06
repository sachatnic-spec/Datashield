import { useState } from "react";

const competitors = [
  { name: "OneTrust", origin: "USA", color: "#6B7280" },
  { name: "TrustArc", origin: "USA", color: "#6B7280" },
  { name: "Securiti.ai", origin: "USA", color: "#6B7280" },
  { name: "Leegality", origin: "India", color: "#9CA3AF" },
  { name: "Signzy", origin: "India", color: "#9CA3AF" },
];

const featureGroups = [
  {
    group: "DPDP Compliance Core",
    icon: "⚖️",
    features: [
      {
        label: "100% DPDP Act Coverage (§4–§20)",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "All competitors have <65% coverage",
      },
      {
        label: "DPBI 72-hr Notification Automation",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "None support DPBI portal integration",
      },
      {
        label: "Section-wise Compliance Checker",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "Manual or generic only",
      },
      {
        label: "DPAR Auto-Generation",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "Spreadsheet-based at all competitors",
      },
      {
        label: "Children's Consent (§9) + Age Gate",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "No competitor has DPDP §9 flow",
      },
      {
        label: "Deemed Consent Tracking (§7)",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "Completely absent market-wide",
      },
      {
        label: "Nomination Right (§14A)",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "New right — zero market coverage",
      },
    ],
  },
  {
    group: "India-Native Infrastructure",
    icon: "🇮🇳",
    features: [
      {
        label: "100% India Data Residency (Mumbai + Hyderabad)",
        datashield: true,
        competitors: [false, false, "partial", false, false],
        gap: "OneTrust/TrustArc store data in US",
      },
      {
        label: "MeitY Cloud Empanelment Pathway",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "No global player is MeitY-empanelled",
      },
      {
        label: "INR Pricing (No Forex Risk)",
        datashield: true,
        competitors: [false, false, false, true, true],
        gap: "Global players charge USD; 60-70% costlier",
      },
      {
        label: "22 Indian Languages for Consent Notices",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "English-only at all global platforms",
      },
      {
        label: "DigiLocker / UIDAI Integration",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "No competitor has Aadhaar/DigiLocker hooks",
      },
      {
        label: "Razorpay / Finacle / Temenos Connectors",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "India banking core integrations absent",
      },
      {
        label: "GeM / Government Procurement Ready",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "No global player can bid on GeM",
      },
    ],
  },
  {
    group: "AI Intelligence",
    icon: "🤖",
    features: [
      {
        label: "DPDP-Specific LLM Policy Gap Analysis",
        datashield: true,
        competitors: [false, false, "partial", false, false],
        gap: "Generic GDPR-tuned AI, not DPDP-native",
      },
      {
        label: "50+ PII Entity Types (Indian formats)",
        datashield: true,
        competitors: [false, false, "partial", false, false],
        gap: "Aadhaar, PAN, ABHA, UPI not detected",
      },
      {
        label: "Real-time Consent Anomaly Detection",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "Batch-only or absent",
      },
      {
        label: "Breach Impact AI Scoring",
        datashield: true,
        competitors: [false, false, "partial", false, false],
        gap: "Manual assessment everywhere else",
      },
      {
        label: "RAG on DPDP Act + Rules + Case Law",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "No RAG on Indian privacy corpus",
      },
      {
        label: "Vendor Risk AI Scoring (DPA-based)",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "Questionnaire-only at competitors",
      },
    ],
  },
  {
    group: "Architecture & Scale",
    icon: "⚙️",
    features: [
      {
        label: "Kubernetes-Native Multi-Tenant SaaS",
        datashield: true,
        competitors: [true, true, true, false, false],
        gap: "Leegality/Signzy are monolithic",
      },
      {
        label: "100K TPS Consent Event Throughput",
        datashield: true,
        competitors: [true, false, true, false, false],
        gap: "Indian players cannot scale to this",
      },
      {
        label: "500M+ Data Principals Support",
        datashield: true,
        competitors: [true, true, true, false, false],
        gap: "Indian players cap at millions",
      },
      {
        label: "BPMN 2.0 Visual Workflow Designer",
        datashield: true,
        competitors: [true, false, false, false, false],
        gap: "Only OneTrust has this; not DPDP-aware",
      },
      {
        label: "Immutable Hash-Chained Audit Logs",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "Mutable logs = weak legal evidence",
      },
      {
        label: "On-Premise Hybrid (K8s Operator)",
        datashield: true,
        competitors: [true, false, false, false, false],
        gap: "Critical for Govt & BFSI mandates",
      },
    ],
  },
  {
    group: "Cross-Sector Regulatory",
    icon: "🏛️",
    features: [
      {
        label: "RBI Cyber Security Framework Overlay",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "No competitor maps to RBI CSF + DPDP",
      },
      {
        label: "SEBI CSCRF + DPDP Combined Module",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "SEBI-specific compliance absent everywhere",
      },
      {
        label: "IRDAI Insurance Data Module",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "Insurance sector unaddressed",
      },
      {
        label: "CERT-In Direction 2022 Integration",
        datashield: true,
        competitors: [false, false, false, false, false],
        gap: "No breach tool links to CERT-In",
      },
      {
        label: "GDPR + DPDP Dual Compliance (IT/ITeS)",
        datashield: true,
        competitors: [true, true, true, false, false],
        gap: "Global players do GDPR but not DPDP",
      },
    ],
  },
];

const mindMapData = {
  label: "DataShield India",
  color: "#10B981",
  children: [
    {
      label: "Consent Management Engine",
      color: "#3B82F6",
      icon: "✅",
      children: [
        { label: "Web / Mobile SDK", color: "#60A5FA" },
        { label: "22-Language Notices", color: "#60A5FA" },
        { label: "Children's Consent §9", color: "#60A5FA" },
        { label: "Consent Preference Centre", color: "#60A5FA" },
        { label: "WhatsApp / OCR Channel", color: "#60A5FA" },
      ],
    },
    {
      label: "Data Principal Rights Engine",
      color: "#8B5CF6",
      icon: "👤",
      children: [
        { label: "Access §11", color: "#A78BFA" },
        { label: "Correction §12", color: "#A78BFA" },
        { label: "Erasure §13", color: "#A78BFA" },
        { label: "Nomination §14A", color: "#A78BFA" },
        { label: "Grievance → DPBI", color: "#A78BFA" },
      ],
    },
    {
      label: "Breach Notification System",
      color: "#EF4444",
      icon: "🚨",
      children: [
        { label: "DPBI 72-hr Automation", color: "#FCA5A5" },
        { label: "AI Harm Scoring", color: "#FCA5A5" },
        { label: "SIEM Integration", color: "#FCA5A5" },
        { label: "Multi-channel DP Alerts", color: "#FCA5A5" },
      ],
    },
    {
      label: "AI Intelligence Engine",
      color: "#F59E0B",
      icon: "🤖",
      children: [
        { label: "PII Detection (50+ types)", color: "#FCD34D" },
        { label: "RAG on DPDP Act", color: "#FCD34D" },
        { label: "Policy Gap Analysis", color: "#FCD34D" },
        { label: "Risk Score (XGBoost)", color: "#FCD34D" },
        { label: "Anomaly Detection", color: "#FCD34D" },
      ],
    },
    {
      label: "Vendor Risk Management",
      color: "#EC4899",
      icon: "🤝",
      children: [
        { label: "DPA Lifecycle", color: "#F9A8D4" },
        { label: "AI Risk Scoring", color: "#F9A8D4" },
        { label: "Sub-processor Tracking", color: "#F9A8D4" },
        { label: "Cross-border Transfer", color: "#F9A8D4" },
      ],
    },
    {
      label: "Platform & Architecture",
      color: "#14B8A6",
      icon: "⚙️",
      children: [
        { label: "30 Microservices (Spring Boot)", color: "#5EEAD4" },
        { label: "Kafka Event Streams", color: "#5EEAD4" },
        { label: "Multi-tenant K8s (EKS)", color: "#5EEAD4" },
        { label: "India Data Residency (AWS)", color: "#5EEAD4" },
        { label: "Zero-trust + OPA ABAC", color: "#5EEAD4" },
        { label: "Immutable Audit Logs", color: "#5EEAD4" },
      ],
    },
  ],
};

const ICON_MAP = {
  "DPDP Compliance Core": "⚖️",
  "India-Native Infrastructure": "🇮🇳",
  "AI Intelligence": "🤖",
  "Architecture & Scale": "⚙️",
  "Cross-Sector Regulatory": "🏛️",
};

export default function App() {
  const [activeTab, setActiveTab] = useState("gap");
  const [expandedGroup, setExpandedGroup] = useState(null);
  const [tooltip, setTooltip] = useState(null);

  const totalDatashield = featureGroups.reduce((a, g) => a + g.features.length, 0);
  const totalCompetitorAvg = featureGroups.reduce((a, g) => {
    return (
      a +
      g.features.reduce((fa, f) => {
        const count = f.competitors.filter((c) => c === true).length;
        return fa + count / f.competitors.length;
      }, 0)
    );
  }, 0);
  const avgCompetitorCoverage = Math.round((totalCompetitorAvg / totalDatashield) * 100);

  return (
    <div style={{
      fontFamily: "'DM Sans', 'Segoe UI', sans-serif",
      background: "#0A0F1E",
      minHeight: "100vh",
      color: "#E2E8F0",
      padding: "0",
    }}>
      {/* Header */}
      <div style={{
        background: "linear-gradient(135deg, #0D1B3E 0%, #0A2A1A 100%)",
        borderBottom: "1px solid rgba(16,185,129,0.2)",
        padding: "24px 32px",
        display: "flex",
        alignItems: "center",
        gap: "16px",
      }}>
        <div style={{
          width: "44px", height: "44px",
          background: "linear-gradient(135deg, #10B981, #059669)",
          borderRadius: "10px",
          display: "flex", alignItems: "center", justifyContent: "center",
          fontSize: "22px", boxShadow: "0 0 20px rgba(16,185,129,0.4)",
        }}>🛡️</div>
        <div>
          <div style={{ fontSize: "20px", fontWeight: "700", color: "#10B981", letterSpacing: "0.5px" }}>
            DataShield India
          </div>
          <div style={{ fontSize: "12px", color: "#64748B", marginTop: "2px" }}>
            Competitive Intelligence + Platform Architecture
          </div>
        </div>
        <div style={{ marginLeft: "auto", display: "flex", gap: "8px" }}>
          {["gap", "mindmap"].map((tab) => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              style={{
                padding: "8px 20px",
                borderRadius: "8px",
                border: "none",
                cursor: "pointer",
                fontWeight: "600",
                fontSize: "13px",
                transition: "all 0.2s",
                background: activeTab === tab
                  ? "linear-gradient(135deg, #10B981, #059669)"
                  : "rgba(255,255,255,0.05)",
                color: activeTab === tab ? "#fff" : "#94A3B8",
                boxShadow: activeTab === tab ? "0 0 15px rgba(16,185,129,0.3)" : "none",
              }}
            >
              {tab === "gap" ? "📊 Market Gap Analysis" : "🗺️ System Mind Map"}
            </button>
          ))}
        </div>
      </div>

      {/* ── TAB 1: GAP ANALYSIS ── */}
      {activeTab === "gap" && (
        <div style={{ padding: "28px 32px" }}>
          {/* Summary bar */}
          <div style={{
            display: "grid",
            gridTemplateColumns: "repeat(4, 1fr)",
            gap: "16px",
            marginBottom: "28px",
          }}>
            {[
              { label: "DataShield Features", value: totalDatashield, sub: "Across 5 domains", color: "#10B981" },
              { label: "Avg Competitor Coverage", value: `${avgCompetitorCoverage}%`, sub: "Of DataShield capabilities", color: "#EF4444" },
              { label: "Exclusive to DataShield", value: "28+", sub: "Zero competitor match", color: "#F59E0B" },
              { label: "Market Gap Advantage", value: "~65%", sub: "Features uncovered in market", color: "#8B5CF6" },
            ].map((s) => (
              <div key={s.label} style={{
                background: "rgba(255,255,255,0.03)",
                border: `1px solid ${s.color}33`,
                borderRadius: "12px",
                padding: "16px 20px",
                borderLeft: `4px solid ${s.color}`,
              }}>
                <div style={{ fontSize: "28px", fontWeight: "800", color: s.color }}>{s.value}</div>
                <div style={{ fontSize: "13px", fontWeight: "600", color: "#CBD5E1", marginTop: "4px" }}>{s.label}</div>
                <div style={{ fontSize: "11px", color: "#475569", marginTop: "2px" }}>{s.sub}</div>
              </div>
            ))}
          </div>

          {/* Legend */}
          <div style={{
            display: "flex", gap: "20px", marginBottom: "20px",
            padding: "12px 20px",
            background: "rgba(255,255,255,0.02)",
            borderRadius: "8px", border: "1px solid rgba(255,255,255,0.05)",
            alignItems: "center",
          }}>
            <span style={{ fontSize: "12px", color: "#64748B", fontWeight: "600" }}>LEGEND:</span>
            {[
              { icon: "✅", label: "Fully supported", bg: "rgba(16,185,129,0.15)", border: "#10B981" },
              { icon: "⚡", label: "Partial / Limited", bg: "rgba(245,158,11,0.15)", border: "#F59E0B" },
              { icon: "❌", label: "Not supported", bg: "rgba(239,68,68,0.1)", border: "#EF4444" },
            ].map((l) => (
              <div key={l.label} style={{
                display: "flex", alignItems: "center", gap: "6px",
                padding: "4px 10px", borderRadius: "6px",
                background: l.bg, border: `1px solid ${l.border}44`,
                fontSize: "12px", color: "#CBD5E1",
              }}>
                <span>{l.icon}</span>{l.label}
              </div>
            ))}
            <span style={{ marginLeft: "auto", fontSize: "11px", color: "#475569" }}>
              Hover gap cells for insight →
            </span>
          </div>

          {/* Table */}
          {featureGroups.map((group) => (
            <div key={group.group} style={{ marginBottom: "20px" }}>
              {/* Group Header */}
              <div
                onClick={() => setExpandedGroup(expandedGroup === group.group ? null : group.group)}
                style={{
                  background: "rgba(16,185,129,0.08)",
                  border: "1px solid rgba(16,185,129,0.2)",
                  borderRadius: expandedGroup === group.group ? "10px 10px 0 0" : "10px",
                  padding: "12px 20px",
                  cursor: "pointer",
                  display: "flex", alignItems: "center", gap: "10px",
                  userSelect: "none",
                }}
              >
                <span style={{ fontSize: "18px" }}>{group.icon}</span>
                <span style={{ fontWeight: "700", fontSize: "14px", color: "#10B981" }}>
                  {group.group}
                </span>
                <span style={{
                  marginLeft: "8px", fontSize: "11px",
                  background: "rgba(16,185,129,0.15)", color: "#10B981",
                  padding: "2px 8px", borderRadius: "20px",
                }}>
                  {group.features.length} features
                </span>
                <span style={{ marginLeft: "auto", color: "#64748B", fontSize: "18px" }}>
                  {expandedGroup === group.group ? "▲" : "▼"}
                </span>
              </div>

              {expandedGroup === group.group && (
                <div style={{
                  border: "1px solid rgba(16,185,129,0.2)",
                  borderTop: "none",
                  borderRadius: "0 0 10px 10px",
                  overflow: "hidden",
                }}>
                  {/* Column headers */}
                  <div style={{
                    display: "grid",
                    gridTemplateColumns: "2.5fr 1fr 1fr 1fr 1fr 1fr 1fr",
                    background: "rgba(0,0,0,0.4)",
                    padding: "8px 16px",
                    fontSize: "11px", fontWeight: "700",
                    color: "#64748B", textTransform: "uppercase", letterSpacing: "0.8px",
                    borderBottom: "1px solid rgba(255,255,255,0.05)",
                  }}>
                    <div>Feature</div>
                    <div style={{ textAlign: "center", color: "#10B981" }}>DataShield</div>
                    {competitors.map((c) => (
                      <div key={c.name} style={{ textAlign: "center" }}>{c.name}</div>
                    ))}
                  </div>

                  {group.features.map((f, fi) => (
                    <div
                      key={fi}
                      style={{
                        display: "grid",
                        gridTemplateColumns: "2.5fr 1fr 1fr 1fr 1fr 1fr 1fr",
                        padding: "10px 16px",
                        background: fi % 2 === 0 ? "rgba(255,255,255,0.01)" : "transparent",
                        borderBottom: "1px solid rgba(255,255,255,0.04)",
                        alignItems: "center",
                      }}
                    >
                      <div style={{ fontSize: "13px", color: "#CBD5E1" }}>{f.label}</div>
                      {/* DataShield cell */}
                      <div style={{ textAlign: "center" }}>
                        <span style={{
                          display: "inline-block",
                          background: "rgba(16,185,129,0.15)",
                          border: "1px solid rgba(16,185,129,0.4)",
                          borderRadius: "6px", padding: "3px 10px",
                          fontSize: "14px",
                        }}>✅</span>
                      </div>
                      {/* Competitor cells */}
                      {f.competitors.map((val, ci) => {
                        const isTrue = val === true;
                        const isPartial = val === "partial";
                        const isFalse = val === false;
                        return (
                          <div
                            key={ci}
                            style={{ textAlign: "center", position: "relative" }}
                            onMouseEnter={() => isFalse && setTooltip({ text: f.gap, x: ci, y: fi + group.group })}
                            onMouseLeave={() => setTooltip(null)}
                          >
                            <span style={{
                              display: "inline-block",
                              background: isTrue
                                ? "rgba(16,185,129,0.1)"
                                : isPartial
                                ? "rgba(245,158,11,0.1)"
                                : "rgba(239,68,68,0.07)",
                              border: `1px solid ${isTrue ? "rgba(16,185,129,0.3)" : isPartial ? "rgba(245,158,11,0.3)" : "rgba(239,68,68,0.2)"}`,
                              borderRadius: "6px", padding: "3px 10px",
                              fontSize: "13px",
                              cursor: isFalse ? "help" : "default",
                            }}>
                              {isTrue ? "✅" : isPartial ? "⚡" : "❌"}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  ))}

                  {/* Gap summary row */}
                  <div style={{
                    background: "rgba(16,185,129,0.04)",
                    padding: "10px 16px",
                    display: "flex", alignItems: "center", gap: "10px",
                    borderTop: "1px solid rgba(16,185,129,0.15)",
                  }}>
                    <span style={{ fontSize: "11px", fontWeight: "700", color: "#64748B", textTransform: "uppercase" }}>
                      Group market gaps →
                    </span>
                    <div style={{ display: "flex", gap: "8px", flexWrap: "wrap" }}>
                      {group.features
                        .filter((f) => f.competitors.every((c) => c === false))
                        .slice(0, 3)
                        .map((f) => (
                          <span key={f.label} style={{
                            fontSize: "11px", color: "#F59E0B",
                            background: "rgba(245,158,11,0.1)",
                            border: "1px solid rgba(245,158,11,0.2)",
                            padding: "3px 8px", borderRadius: "4px",
                          }}>
                            🏆 {f.label}
                          </span>
                        ))}
                      <span style={{ fontSize: "11px", color: "#475569" }}>
                        (❌ from all 5 competitors)
                      </span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          ))}

          {/* Bottom insight */}
          <div style={{
            marginTop: "24px",
            background: "linear-gradient(135deg, rgba(16,185,129,0.08), rgba(59,130,246,0.05))",
            border: "1px solid rgba(16,185,129,0.2)",
            borderRadius: "12px", padding: "20px 24px",
            display: "flex", gap: "32px",
          }}>
            <div>
              <div style={{ fontSize: "13px", fontWeight: "700", color: "#10B981", marginBottom: "6px" }}>
                🎯 Primary Moat
              </div>
              <div style={{ fontSize: "12px", color: "#94A3B8", lineHeight: "1.6" }}>
                No competitor combines <strong style={{color:"#E2E8F0"}}>DPDP-native design + India data residency + 
                vernacular consent + DPBI automation</strong>. DataShield is the only platform 
                where all four coexist.
              </div>
            </div>
            <div>
              <div style={{ fontSize: "13px", fontWeight: "700", color: "#F59E0B", marginBottom: "6px" }}>
                ⚡ First-Mover Window
              </div>
              <div style={{ fontSize: "12px", color: "#94A3B8", lineHeight: "1.6" }}>
                Global players need <strong style={{color:"#E2E8F0"}}>18–24 months</strong> to localise. 
                Indian players (Leegality, Signzy) lack full-stack compliance depth. 
                Window: <strong style={{color:"#E2E8F0"}}>2025–2026 enforcement phase.</strong>
              </div>
            </div>
            <div>
              <div style={{ fontSize: "13px", fontWeight: "700", color: "#8B5CF6", marginBottom: "6px" }}>
                💡 Whitespace to Own
              </div>
              <div style={{ fontSize: "12px", color: "#94A3B8", lineHeight: "1.6" }}>
                CERT-In + DPBI dual integration, RBI/SEBI overlay modules, 
                and <strong style={{color:"#E2E8F0"}}>GeM procurement readiness</strong> — 
                completely unaddressed by any existing player.
              </div>
            </div>
          </div>
        </div>
      )}

      {/* ── TAB 2: MIND MAP ── */}
      {activeTab === "mindmap" && (
        <div style={{ padding: "28px 32px" }}>
          <div style={{
            fontSize: "13px", color: "#64748B",
            marginBottom: "20px",
            display: "flex", gap: "24px",
          }}>
            <span>🟢 Core Platform</span>
            <span>🔵 Consent Module</span>
            <span>🟣 Rights Engine</span>
            <span>🔴 Breach System</span>
            <span>🟡 AI Engine</span>
            <span>🩷 Vendor Mgmt</span>
            <span>🩵 Architecture</span>
          </div>

          {/* Center Hub */}
          <div style={{ display: "flex", justifyContent: "center", marginBottom: "32px" }}>
            <div style={{
              background: "linear-gradient(135deg, #10B981, #059669)",
              borderRadius: "16px",
              padding: "20px 40px",
              textAlign: "center",
              boxShadow: "0 0 40px rgba(16,185,129,0.4)",
            }}>
              <div style={{ fontSize: "28px", marginBottom: "6px" }}>🛡️</div>
              <div style={{ fontSize: "18px", fontWeight: "800", color: "#fff" }}>DataShield India</div>
              <div style={{ fontSize: "11px", color: "rgba(255,255,255,0.7)", marginTop: "4px" }}>
                DPDP Compliance SaaS Platform
              </div>
              <div style={{ display: "flex", gap: "8px", marginTop: "10px", justifyContent: "center" }}>
                {["Java Spring Boot", "Angular", "K8s", "AWS India"].map((t) => (
                  <span key={t} style={{
                    fontSize: "10px", background: "rgba(0,0,0,0.25)",
                    padding: "2px 8px", borderRadius: "4px", color: "rgba(255,255,255,0.8)",
                  }}>{t}</span>
                ))}
              </div>
            </div>
          </div>

          {/* Module Grid */}
          <div style={{
            display: "grid",
            gridTemplateColumns: "repeat(3, 1fr)",
            gap: "20px",
          }}>
            {mindMapData.children.map((module) => (
              <div key={module.label} style={{
                background: `${module.color}10`,
                border: `1px solid ${module.color}33`,
                borderRadius: "12px",
                overflow: "hidden",
              }}>
                {/* Module header */}
                <div style={{
                  background: `${module.color}20`,
                  borderBottom: `1px solid ${module.color}33`,
                  padding: "14px 18px",
                  display: "flex", alignItems: "center", gap: "10px",
                }}>
                  <span style={{ fontSize: "20px" }}>{module.icon}</span>
                  <div>
                    <div style={{ fontSize: "13px", fontWeight: "700", color: module.color }}>
                      {module.label}
                    </div>
                  </div>
                </div>

                {/* Sub-features */}
                <div style={{ padding: "12px 16px", display: "flex", flexDirection: "column", gap: "8px" }}>
                  {module.children.map((child) => (
                    <div key={child.label} style={{
                      display: "flex", alignItems: "center", gap: "8px",
                    }}>
                      <div style={{
                        width: "6px", height: "6px",
                        borderRadius: "50%",
                        background: module.color,
                        flexShrink: 0,
                      }} />
                      <span style={{ fontSize: "12px", color: "#CBD5E1" }}>{child.label}</span>
                    </div>
                  ))}
                </div>
              </div>
            ))}
          </div>

          {/* Architecture Flow */}
          <div style={{ marginTop: "28px" }}>
            <div style={{
              fontSize: "13px", fontWeight: "700", color: "#64748B",
              textTransform: "uppercase", letterSpacing: "0.8px",
              marginBottom: "14px",
            }}>
              📐 Data + Event Flow Architecture
            </div>
            <div style={{
              display: "flex",
              alignItems: "center",
              gap: "0",
              overflowX: "auto",
              background: "rgba(255,255,255,0.02)",
              border: "1px solid rgba(255,255,255,0.06)",
              borderRadius: "12px",
              padding: "20px 24px",
            }}>
              {[
                { label: "Data Principal", sub: "Web/Mobile/WA/Email", color: "#3B82F6", icon: "👤" },
                { label: "API Gateway", sub: "Kong + AWS WAF", color: "#8B5CF6", icon: "🔀" },
                { label: "Microservices", sub: "30 Spring Boot SVCs", color: "#10B981", icon: "⚙️" },
                { label: "Kafka", sub: "Event Streaming", color: "#F59E0B", icon: "📨" },
                { label: "PostgreSQL", sub: "Tenant Schema Isolated", color: "#EF4444", icon: "🗄️" },
                { label: "Elasticsearch", sub: "Audit + Search", color: "#EC4899", icon: "🔍" },
                { label: "AI Engine", sub: "LLM + RAG + ML", color: "#14B8A6", icon: "🤖" },
              ].map((node, i, arr) => (
                <div key={node.label} style={{ display: "flex", alignItems: "center", flexShrink: 0 }}>
                  <div style={{
                    background: `${node.color}15`,
                    border: `1px solid ${node.color}44`,
                    borderRadius: "10px",
                    padding: "12px 16px",
                    textAlign: "center",
                    minWidth: "110px",
                  }}>
                    <div style={{ fontSize: "20px", marginBottom: "4px" }}>{node.icon}</div>
                    <div style={{ fontSize: "12px", fontWeight: "700", color: node.color }}>{node.label}</div>
                    <div style={{ fontSize: "10px", color: "#475569", marginTop: "2px" }}>{node.sub}</div>
                  </div>
                  {i < arr.length - 1 && (
                    <div style={{
                      display: "flex", flexDirection: "column", alignItems: "center",
                      margin: "0 4px",
                    }}>
                      <div style={{ fontSize: "16px", color: "#334155" }}>→</div>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>

          {/* Compliance Coverage Map */}
          <div style={{ marginTop: "24px" }}>
            <div style={{
              fontSize: "13px", fontWeight: "700", color: "#64748B",
              textTransform: "uppercase", letterSpacing: "0.8px",
              marginBottom: "14px",
            }}>
              ⚖️ DPDP Section Coverage Map
            </div>
            <div style={{
              display: "flex", flexWrap: "wrap", gap: "8px",
              background: "rgba(255,255,255,0.02)",
              border: "1px solid rgba(255,255,255,0.06)",
              borderRadius: "12px",
              padding: "20px 24px",
            }}>
              {[
                { sec: "§4", label: "Grounds for Processing", mod: "Consent Engine" },
                { sec: "§5", label: "Notice to DP", mod: "Policy Manager" },
                { sec: "§6", label: "Consent Management", mod: "CME" },
                { sec: "§7", label: "Deemed Consent", mod: "CME" },
                { sec: "§8", label: "Fiduciary Obligations", mod: "BNS + Audit" },
                { sec: "§8(2)", label: "Processor Obligations", mod: "Vendor Risk" },
                { sec: "§8(5)", label: "Security Safeguards", mod: "Data Discovery" },
                { sec: "§9", label: "Children's Data", mod: "CME §9 Module" },
                { sec: "§10", label: "SDF Obligations", mod: "SDF Module" },
                { sec: "§11", label: "Right of Access", mod: "DPRE" },
                { sec: "§12", label: "Right to Correction", mod: "DPRE" },
                { sec: "§13", label: "Right to Erasure", mod: "DPRE" },
                { sec: "§14", label: "Grievance Redressal", mod: "Grievance Module" },
                { sec: "§14A", label: "Nomination Right", mod: "DPRE" },
                { sec: "§16", label: "DPBI Integration", mod: "DPBI Service" },
                { sec: "§17", label: "Adjudication Support", mod: "Evidence Generator" },
                { sec: "§19–20", label: "Penalty Framework", mod: "Risk Calculator" },
              ].map((item) => (
                <div key={item.sec} style={{
                  background: "rgba(16,185,129,0.08)",
                  border: "1px solid rgba(16,185,129,0.25)",
                  borderRadius: "8px",
                  padding: "8px 12px",
                  minWidth: "140px",
                }}>
                  <div style={{ fontSize: "12px", fontWeight: "700", color: "#10B981" }}>{item.sec}</div>
                  <div style={{ fontSize: "11px", color: "#94A3B8", marginTop: "2px" }}>{item.label}</div>
                  <div style={{
                    fontSize: "10px", color: "#64748B",
                    marginTop: "4px",
                    background: "rgba(0,0,0,0.2)",
                    padding: "2px 6px", borderRadius: "4px",
                    display: "inline-block",
                  }}>
                    → {item.mod}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Financial Snapshot */}
          <div style={{
            marginTop: "24px",
            display: "grid",
            gridTemplateColumns: "repeat(5, 1fr)",
            gap: "12px",
          }}>
            {[
              { year: "Y1", arr: "₹8.5Cr", customers: "85", ebitda: "-74%", color: "#EF4444" },
              { year: "Y2", arr: "₹27Cr", customers: "270", ebitda: "+7%", color: "#F59E0B" },
              { year: "Y3", arr: "₹64Cr", customers: "640", ebitda: "+36%", color: "#10B981" },
              { year: "Y4", arr: "₹128Cr", customers: "1200", ebitda: "+52%", color: "#10B981" },
              { year: "Y5", arr: "₹240Cr", customers: "2000", ebitda: "+63%", color: "#10B981" },
            ].map((y) => (
              <div key={y.year} style={{
                background: `${y.color}10`,
                border: `1px solid ${y.color}33`,
                borderRadius: "10px",
                padding: "14px 16px",
                textAlign: "center",
              }}>
                <div style={{ fontSize: "11px", color: "#64748B", fontWeight: "700", marginBottom: "6px" }}>
                  {y.year}
                </div>
                <div style={{ fontSize: "20px", fontWeight: "800", color: y.color }}>{y.arr}</div>
                <div style={{ fontSize: "11px", color: "#94A3B8", marginTop: "4px" }}>ARR</div>
                <div style={{ fontSize: "12px", color: "#CBD5E1", marginTop: "6px" }}>{y.customers} customers</div>
                <div style={{
                  fontSize: "11px", marginTop: "4px",
                  color: y.ebitda.startsWith("+") ? "#10B981" : "#EF4444",
                }}>
                  EBITDA {y.ebitda}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
