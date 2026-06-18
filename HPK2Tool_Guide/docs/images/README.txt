HPK2Tool Guide — docs/images/
===============================================================================
Place GUI screenshots in this directory.
Each screenshot file is referenced by gui.html as an <img> tag.
When a file is present, it replaces the placeholder box automatically.

-------------------------------------------------------------------------------
GUIDE STRUCTURE
-------------------------------------------------------------------------------

  HPK2Tool_Guide/
  ├── index.html          Main hub
  │                         • Prerequisites: Windows / Linux / macOS (tabbed)
  │                             - WSL 1 + Ubuntu 20.04 setup (Windows)
  │                             - .NET Desktop Runtime 8 install steps (all OS)
  │                             - OpenSSL note for macOS
  │                         • ASCII architecture diagram
  │                             (CLI/GUI → Common Library → Device pipeline)
  │                         • Getting Started: executables table, verify commands
  │                         • Bundle format comparison (.bdl vs .hpk)
  │
  ├── cli.html            CLI Reference Guide
  │                         • All 10 commands with option tables
  │                             (Required / Optional clearly marked)
  │                         • Code examples per command:
  │                             PowerShell / CMD / Linux tabs
  │                         • Reference tables:
  │                             - Platform Version table (19.3 – 31.9)
  │                             - Localization JSON format
  │                             - USB Accessory format
  │                             - Web Service endpoint format
  │                         • End-to-End Scenarios:
  │                             - Scenario 1: Simple Scanner App
  │                               (create → install → solution-list)
  │                             - Scenario 2: Enterprise Suite
  │                               (create → install → config-update
  │                                → attestation-update)
  │
  ├── gui.html            GUI Reference Guide
  │                         • ASCII screen navigation flowchart
  │                             (menu bar → all screens → dialogs)
  │                         • Menu bar reference table
  │                         • All 15 screens documented:
  │                             - Solution Builder (Solution Tab,
  │                               Build Option Tab, Agent Tabs,
  │                               Web Service Tab, Accessory Tab)
  │                             - Application Dialog
  │                             - Accessory Dialog
  │                             - Web Service Endpoint Dialog
  │                             - Localization Dialog
  │                             - Icon Set Dialog
  │                             - Install / Uninstall Screen
  │                             - Management Screen
  │                             - Solution Detail (read-only)
  │                             - Configuration Screen
  │                             - Attestation Screen
  │                             - About Dialog
  │                         • Field description tables per screen
  │                           (Required / Optional clearly marked)
  │                         • 8-step End-to-End workflow walkthrough
  │                             (build → install → verify)
  │
  ├── style.css           Shared stylesheet
  │                         • HP brand colors, sidebar layout
  │                         • OS tabs, code tabs (vanilla JS)
  │                         • Callout boxes, step lists, tables
  │                         • Screenshot placeholders (auto-hide on load)
  │
  └── docs/images/        ← YOU ARE HERE
      └── README.txt      Screenshot naming convention and guide structure

-------------------------------------------------------------------------------
SCREENSHOT NAMING CONVENTION
-------------------------------------------------------------------------------

  gui_solution_builder.png        — Solution Builder, Solution Tab
  gui_solution_builder_auth.png   — Solution Builder, Authentication section
  gui_solution_builder_build.png  — Solution Builder, Build Option Tab
  gui_application_dialog.png      — Application Detail Dialog
  gui_accessory_dialog.png        — Accessory Dialog
  gui_webservice_dialog.png       — Web Service Endpoint Dialog
  gui_localization_dialog.png     — Localization Editor Dialog
  gui_iconset_dialog.png          — Icon Set Dialog
  gui_install.png                 — Install / Uninstall Screen
  gui_management.png              — Management Screen
  gui_management_detail.png       — Solution Detail (read-only)
  gui_configuration.png           — Configuration Screen
  gui_attestation.png             — Attestation Screen
  gui_about.png                   — About Dialog

-------------------------------------------------------------------------------
HOW TO ADD SCREENSHOTS
-------------------------------------------------------------------------------

  1. Launch the HP Workpath Solution Utility GUI.
  2. Navigate to each screen listed above.
  3. Capture a screenshot (recommended size: 650–900px wide).
  4. Save the file using the exact filename from the naming convention above
     and place it in this directory (docs/images/).
  5. Reload gui.html in a browser — the placeholder box is automatically
     replaced by the actual screenshot image.
