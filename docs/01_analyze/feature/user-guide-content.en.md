# User Guide Content (FEAT-COM-002)

> Domain: common · Version: 0.2 · Written: 2026-07-11 (English translation added 2026-07-12, maintenance request)
> This is the content of the dedicated user guide screen reached by clicking the "?" icon in the header (REQ-COM-002). Written in Markdown; the screen either renders this document as-is or converts it into data with the same structure. It consists of 3 sections (tabs): Overview, Domains & Principles, and Role-by-Role Guide.
>
> Basis: Domain overviews/principles are based on `docs/01_analyze/prd/*.md`, role personas/access scope are based on `docs/02_plan/security/authorization/*.md`, actual menu names are from `source/frontend/src/routes/navConfig.tsx`, and actual screen/button names are from `docs/02_plan/screen/*.md`. Menu and button names quote the actual implementation names verbatim and are not paraphrased.

---

## 1. Overview

This ITSM (IT Service Management) platform is designed so that an organization's entire IT operations and internal service support work can be handled within a single system.

In addition to the service desk functions (service requests, incidents, problems, changes, knowledge, assets) traditionally used only by the IT department, it can also handle internal service requests from non-IT departments such as HR, Legal, Facilities, and Finance in the same way (Enterprise Service Management, ESM). On top of this, the platform also covers security vulnerability management, regulatory/policy compliance management, and infrastructure uptime/capacity monitoring — all within a single platform.

There are three principles worth knowing before using the platform.

First, **all work follows the common flow of register → process → track.** Whether it is a request, an incident, or a change, it is first registered and then transitions through defined status values, and that process is recorded in a timeline and audit log.

Second, **the screens and features you can access depend on the role of the logged-in user.** This is called role-based access control (RBAC), and the sidebar menu only shows items that your role can access. For example, a Service Desk Agent will see the "Incident" menu, but an End User will not.

Third, **the header is the common entry point shared by every screen.** The universal search in the center of the header lets you search across Knowledge, Service Requests, Incidents, Problems, and Changes at once, crossing domain boundaries. Clicking the notification bell lets you see recent notifications from multiple domains gathered in one place (expiry approaching, threshold exceeded, etc.), and clicking the "?" icon on the right takes you back to this very user guide you are reading now.

Section "2. Domains & Principles" below explains what each of the 11 business domains covered by this platform is for and what core principles it operates by. Section "3. Role-by-Role Guide" uses fictional personas as examples to concretely explain which button on which menu each role uses to do what. Find the item that matches your role and read it, and you can follow along directly on the actual screen.

---

## 2. Domains & Principles

### 2.1 Auth & RBAC

This is the common foundation area for all domains. When you log in with your email and password, the system issues an Access Token (short-lived) and a Refresh Token (long-lived); when the Access Token expires, it automatically attempts to reissue it using the Refresh Token. When you log out, the tokens for that session are immediately invalidated and can no longer be reused.

**Core principle**: Every screen navigation and API request checks the role information contained in the Access Token issued at login. If a screen or feature is requested without access permission, the system returns a 403 (access denied) response without exception. However, only the SYSTEM_ADMIN role is exceptionally allowed to always access the screens and features of every other role.

### 2.2 Service Request Management

This is the self-service window employees use to request access permissions, equipment, information, and more. A request goes through 7 stages: **Submit → Validate → Classify/Route → Approve → Fulfill → Verify → Close**.

**Core principle**: An SLA (response target time, resolution target time) is set per request type, and the system tracks actual processing time against these targets to indicate whether they are approaching or in breach. Only request types defined as requiring approval go through an approval stage; other types are fulfilled immediately without approval.

### 2.3 Incident Management

This is the area for quickly registering and responding to/recovering from unplanned service outages or performance degradation. An incident follows the lifecycle: **New → In Progress → Resolved → Closed**.

**Core principle**: A severity (SEV1–SEV3) is assigned to an incident as the basis for prioritization, and the Incident Manager assigns response roles such as Tech Lead, Communications Manager, and Scribe. After resolution, a blameless postmortem is written to record the root cause and action items, and if needed to prevent recurrence, it is linked to a Problem.

### 2.4 Problem Management

This is the area for finding the root cause of incidents to prevent recurrence. A problem goes through 6 stages: **Detect → Classify/Prioritize → Investigate/Diagnose → Record Known Error → Workaround → Resolve/Close**.

**Core principle**: Priority is calculated as impact × urgency. A workaround (temporary mitigation) can be registered even while the root cause has not yet been removed, and once both the root cause and workaround are documented together, it is recorded as a "Known Error" in the KEDB (Known Error Database) for reuse in resolving similar problems in the future.

### 2.5 Change Management

This is the area for controlling the risk of change work (deployments, configuration changes, etc.) that affects services. A change request (RFC) goes through 6 stages: **Request → Review → Plan → Approve → Implement → Close**.

**Core principle**: Changes are classified into three types — standard, normal, and emergency — and the approval path differs depending on the type and risk level. A standard change (a repeatable task pre-verified as safe) is pre-approved and can proceed without re-approval, but a normal change must go through peer review or CAB (Change Advisory Board) approval depending on risk level, and cannot transition to the implementation stage before approval is complete.

### 2.6 Knowledge Management

This is the area for documenting technical information and resolution know-how to support self-service and agent work. An article goes through the states: **Draft → Review → Published**.

**Core principle**: No matter how good an article a knowledge contributor writes, it does not become published and is not exposed to end users without review and approval by a gatekeeper (quality reviewer). This serves as a quality gate that maintains the accuracy and freshness of the knowledge base. Published articles receive helpfulness ratings ("Was this helpful?") for continuous quality management.

### 2.7 IT Asset Management / CMDB

This is the area for managing the lifecycle (Plan → Procure → Operate → Maintain → Retire) of hardware, software, and cloud assets, and for managing dependency relationships between configuration items (CIs) via the CMDB.

**Core principle**: Assets/CIs can be linked to service request, incident, problem, and change tickets, and through this linkage you can look up the scope affected by a specific change (which other assets/services are affected). When a license, warranty, or contract expiry date approaches, the system automatically generates a notification.

### 2.8 Enterprise Service Management (ESM)

This is the area that extends the same principles as Service Request Management (catalog, workflow, SLA) to non-IT departments such as HR, Legal, Facilities, and Finance. Work that requires multiple departments to move together, such as onboarding (new hire) and offboarding (departure) processing, is managed via checklists.

**Core principle**: An HR case (a sensitive personnel issue) can only transition in the fixed order Intake → Record → Investigate → Resolve, and **only the HR_CASE_MANAGER and SYSTEM_ADMIN roles can access it.** When an onboarding/offboarding request is submitted, the system automatically generates a checklist and automatically assigns subtasks to relevant departments such as IT, Facilities, and HR, and the overall checklist is completed only once all subtasks are completed.

### 2.9 Vulnerability Management

This is the area for systematically tracking security vulnerabilities from discovery through remediation, verification, and reporting. A vulnerability goes through 6 stages: **Discover → Assess → Prioritize → Remediate → Verify → Report**.

**Core principle**: A vulnerability's risk score is calculated considering both severity and exploitability, and this score determines processing priority. **It cannot transition to the Remediation stage while no assignee (owner) has been assigned** — this is a control to ensure clear accountability so that vulnerabilities are not left unaddressed.

### 2.10 Compliance Management

This is the area for registering regulatory obligations or internal policies as requirements and managing compliance.

**Core principle**: Every compliance requirement must have an owner assigned, and requirements without an owner are highlighted as "Owner not assigned" in the list/detail screens. Violations or issues of a requirement are registered as corrective action items and tracked through Detect → In Progress → Resolved states, and all related activity — requirement registration, status changes, action processing, etc. — is recorded in the audit log to prepare for future audits.

### 2.11 IT Infrastructure Monitoring & Capacity Management

This is the area for manually recording the uptime/performance metrics of infrastructure assets such as servers and network equipment, and for planning team-level capacity (demand versus processing capability).

**Core principle**: A threshold (upper/lower bound) can be set for each metric item (uptime, CPU, memory, response time), and when a registered metric value falls outside the threshold, the system immediately generates a notification. An SLA target can be set for uptime, allowing you to compare actual uptime against the target.

> In addition to the 11 business domains above, the header's **universal search** (search across Knowledge, Service Requests, Incidents, Problems, and Changes at once, crossing domains), **notification popover** (gather and check recent notifications from each domain), and **light/dark theme switching** are common features shared by all domains.

---

## 3. Role-by-Role Guide

This section uses fictional personas as examples to concretely explain, for each of the 16 roles defined in the platform, "which button on which menu they use to do what." Menu names are the actual names displayed in the sidebar, and button names are the actual names displayed on the screen.

### 3.1 SYSTEM_ADMIN — System Administrator

**Persona**: Team Lead Park Seo-jun, a system administrator in the IT Infrastructure team. Manages company-wide accounts and permission structures, and investigates security incidents based on audit logs.

Team Lead Park Seo-jun starts work in the sidebar's **"Administrator"** menu group after logging in.

- To create an account for a new hire, he goes into the **"Account Management"** menu (account list screen) and clicks the **"Create Account"** button in the upper right. He enters the email, name, initial role(s) (one or more), and initial password, then clicks the **"Save"** button to create the account; the new hire changes their password themselves after first login.
- When an account for a departing or leave-of-absence employee needs to be blocked, he clicks the target row in the account list to go to the detail screen, then clicks the **"Deactivate"** button. A confirmation dialog appears for a final confirmation before processing, and the account is immediately blocked from logging in once processed.
- For an employee whose role has changed (e.g., moved from Agent to Incident Manager), he adds a new role or revokes an existing role (removes the chip) in the role grant/revoke panel on the same account detail screen.
- When a reorganization requires a new role, he clicks the **"Create Role"** button on the **"Role Management"** menu and enters a role name and description to define the new role.
- During a security check or when a suspicious login is detected, he narrows down the period-based history on the **"Audit Log"** menu using the event type filter (login/logout/reissue/account & role change).

Beyond the screens unique to this role, Team Lead Park Seo-jun can also exceptionally access the screens and features defined for every other role. That is, if needed, he can view and operate any domain screen — incident details, HR cases, compliance requirements, and so on.

### 3.2 END_USER — End User / Requester

**Persona**: Staff member Lee Ha-eun, a new hire in the Marketing team. Logs into the system directly to submit a request whenever she needs a laptop issued or information about a company policy.

When Staff member Lee Ha-eun needs a new monitor, she goes into the **"Service Portal"** menu in the sidebar's **"Service Requests"** group. Clicking the category card for the request type she wants (e.g., "Equipment Issuance Request") takes her to a dynamic form screen; she fills in the required fields and clicks the **"Submit"** button. Related knowledge articles are recommended on the right side of the submission screen, so cases she can resolve on her own without even submitting a request are surfaced.

After submitting, she can check the processing status of her own requests (submitted/pending approval/in fulfillment/closed) and SLA status in the **"My Requests"** menu in the same group. Once a request is closed, a CSAT (satisfaction) widget appears on the detail screen where she can leave a star rating and comment.

When she needs an HR-related request (e.g., issuing an employment certificate), she selects the HR tab in the **"Department Service Portal"** menu of the **"Department Services"** group and submits a request the same way, then tracks progress in the **"My Department Requests"** menu.

When she has a question, she first searches by keyword in the **"Knowledge Base"** menu of the **"Knowledge"** group to view published articles, and can leave a rating using the "Was this helpful?" widget at the bottom of an article.

### 3.3 SERVICE_DESK_AGENT — Service Desk Agent

**Persona**: Assistant Manager Kim Min-su, a first-line responder on the IT service desk. Handles requests and incident reports coming into the queue all day long.

When Assistant Manager Kim Min-su starts his shift, he opens the **"Request Queue"** menu in the sidebar's **"Service Requests"** group. He checks his assigned queue in the queue list on the left, and clicks the **"Assign"** button on a request row to assign it to himself. He goes into the detail screen of an assigned request to communicate with the requester via comments, and once processing is done, clicks a status transition button (e.g., "Fulfilled") to update the status.

When he receives a call reporting a system failure, he clicks the **"Register Incident"** button on the **"Incident"** menu of the **"Incident"** group and enters a summary, severity, and affected service to create an incident. He then leaves progress updates as status updates, and for issues that are difficult to resolve himself, he hands off to a senior handler using the **"Escalate"** button on the detail screen.

When a frequently asked question comes up during a response, he searches the **"Knowledge Base"** menu in the **"Knowledge"** group for a relevant article to use in his answer.

### 3.4 APPROVER — Approver (including CAB Members)

**Persona**: General Manager Jeong Woo-jin, a budget approver in the Finance team and a Change Advisory Board (CAB) member. Approves high-value equipment purchase requests and system change requests.

When General Manager Jeong Woo-jin needs to approve a service request, he goes into the **"Approval Inbox"** menu of the **"Service Requests"** group, clicks an item in the pending list to review the request content, then clicks the **"Approve"** or **"Reject"** button. When rejecting, entering a reason is mandatory.

He processes change request approvals in the **"CAB Approval Inbox"** menu of the **"Change"** group. The higher the risk of a change, the more carefully he reviews it, and likewise he records his decision using the **"Approve"**/**"Reject"** buttons along with an opinion entry.

### 3.5 PROCESS_OWNER — Process Owner

**Persona**: Manager Han Ji-su, in charge of standardizing service operation processes. Creates new request types and manages SLA targets.

Every new-hire onboarding season, Manager Han Ji-su goes into the **"Service Catalog"** menu of the **"Service Requests"** group to organize request types, adding new items to the catalog item list or editing existing items' form fields, whether approval is required, the assigned queue, and SLA targets (response/resolution time).

When a catalog for department services is needed, in the **"Department Catalog Management"** menu of the **"Department Services"** group, she designates the responsible department (HR/Legal/Facilities/Finance) and defines the form fields and, for onboarding/offboarding, the checklist template as well.

Every quarter, she checks CSAT, response/resolution time, SLA compliance rate, and per-department volume in the **"Request Metrics"** and **"ESM Metrics"** menus and reflects them in catalog improvements.

### 3.6 INCIDENT_MANAGER — Incident Manager

**Persona**: Deputy General Manager Choi Do-yun, an IT operations manager who oversees incident response. Organizes the response team and is responsible for preventing recurrence when a failure occurs.

When a serious failure is received, Deputy General Manager Choi Do-yun goes into the detail screen of that item in the **"Incident"** menu of the **"Incident"** group, reconfirms the severity in the severity/priority edit area, and assigns Tech Lead, Communications Manager, and Scribe in the role assignment panel. If the response drags on, he hands it off to a higher organization using the **"Escalate"** button, and specifies internal/external in the status update input box to announce progress.

Once the failure is recovered, he enters the impact start, detection, and impact end times in the resolution area to calculate MTTD/MTTA/MTTR metrics, and for SEV1/SEV2 incidents, goes to the **"Edit Postmortem"** screen to write the 5 Whys, root cause, and action items, then saves with the **"Submit"** button. If recurrence prevention is needed, he uses the **"Link Problem"** button on the detail screen to create a new Problem or link to an existing one.

In the **"Incident Metrics"** menu, he checks period-based counts, severity distribution, and average MTTR to review team performance.

### 3.7 PROBLEM_MANAGER — Problem Manager

**Persona**: Section Chief Yoon Seo-yeon, in charge of Problem Management, tracking the root causes of recurring failures.

When the same failure occurs two or more times, Section Chief Yoon Seo-yeon registers a problem in the **"Problem"** menu of the **"Problem"** group, and records the root cause and 5 Whys analysis in the RCA section of the detail screen. If removing the cause takes time, she leaves a temporary countermeasure in the workaround input field, and once both the root cause and workaround are organized, she clicks the **"Create Known Error"** button to register it in the KEDB.

She bidirectionally links related past incidents using the **"Link Incident"** button, and if a system change is needed for a fundamental fix, links it to a new or existing change request using the **"Link Change"** button. She registers assignees and due dates in the follow-up action list to manage progress, and once the problem is completely removed, clicks the **"Close"** button to finish.

When checking whether a problem with similar symptoms has recurred, she looks up past known errors by keyword in the **"KEDB Search"** menu.

### 3.8 CHANGE_MANAGER — Change Manager

**Persona**: Manager Kang Tae-min, overseeing the system change process. The approval decision itself is an APPROVER/CAB authority; Manager Kang Tae-min coordinates the process and schedule.

When Manager Kang Tae-min receives a request that a deployment or configuration change is needed, he creates a change request (RFC) in the **"Change"** menu of the **"Change"** group, and enters the change type (standard/normal/emergency), risk level, expected implementation, affected systems, and rollback method. The approval path (auto-approved/peer review/CAB) is automatically determined based on the type and risk level.

Once approval is complete, he transitions the process status to "Implementation" on the detail screen (this transition itself is blocked before approval), and once implementation is done, records success/failure and whether a rollback was executed in the implementation result record area. If there are related incidents or problems, he links them using the incident/problem linking buttons.

When coordinating a company-wide deployment schedule, he checks upcoming changes at a glance on the calendar in the **"Change Schedule"** menu, and monitors change success rate, failure rate, and emergency change ratio in the **"Change Metrics"** menu to use in process improvement.

### 3.9 KNOWLEDGE_CONTRIBUTOR — Knowledge Contributor

**Persona**: Staff member Oh Se-hun, who documents resolution know-how for recurring inquiries.

When Staff member Oh Se-hun feels the same inquiry keeps recurring, he goes into the **"Write Article"** menu of the **"Knowledge"** group to write a title and body and assign a category and labels. Once the draft is complete, he clicks the **"Request Review"** button to ask a gatekeeper for review (at this point, the status changes from draft to review). Articles that are no longer valid are removed with the **"Delete"** button.

He also writes new articles on the spot, or links existing articles (KCS), while handling an incident or service request, so that the next user facing the same issue can resolve it quickly.

### 3.10 KNOWLEDGE_GATEKEEPER — Knowledge Gatekeeper

**Persona**: Senior Associate Seo Ji-hun, who manages the quality of the knowledge base as a review lead.

Senior Associate Seo Ji-hun checks the list of articles pending review in the **"Review & Publish Inbox"** menu of the **"Knowledge"** group, clicks an item to preview the content and judge its accuracy and freshness. If he judges it fine to publish, he clicks the **"Approve"** button to transition the article to published status; if it needs improvement, he clicks the **"Reject"** button and enters a reason (a rejected article returns to draft status so the contributor can revise it).

In the **"Knowledge Metrics"** menu, he checks usage, zero-result searches, helpfulness ratings, and ticket deflection rate to identify (via the zero-result search keyword ranking) which topics need more articles.

### 3.11 ASSET_MANAGER — Asset Manager

**Persona**: Associate Im Ha-yun, who manages all of the company's IT equipment and software licenses.

When a new laptop arrives, Associate Im Ha-yun clicks the **"Register Asset"** button in the **"Asset"** menu of the **"Asset"** group, selects the type (HW/SW/Cloud), and enters the owner, location, purchase date, cost, expiry date, and so on. When an asset's status changes (e.g., deployed, under maintenance), she updates it using the lifecycle stage transition button on the detail screen, and equipment that has reached end of life is processed with the **"Retire"** button (requires a confirmation dialog).

When dependency relationships between servers need to be understood, she registers CIs in the **"CI/CMDB Relationships"** menu and links dependency relationships via the relationship-add form, and when checking which assets a specific change affects, she uses the impact scope panel on the same screen. If equipment is related to a specific incident or change, she links it using the **"Link Ticket"** button on the asset detail screen.

Assets with license/warranty expiry approaching are checked in the dashboard of the **"Asset Metrics"** menu to decide whether to renew.

### 3.12 HR_CASE_MANAGER — HR Case Manager

**Persona**: Assistant Manager Namgung Ye-eun, who handles sensitive personnel issues (grievances, disciplinary actions, etc.) in the HR team.

When an employee grievance report comes in, Assistant Manager Namgung Ye-eun goes into the **"HR Cases"** menu of the sidebar's **"HR Cases"** group and clicks the **"Intake Case"** button to create a case. She then clicks status transition buttons on the detail screen to move through the fixed order (Intake → Record → Investigate → Resolve), and that history is left in the status history timeline.

To protect sensitive information, this menu and screen are not exposed at all in the sidebar to anyone other than Assistant Manager Namgung Ye-eun (HR_CASE_MANAGER) and SYSTEM_ADMIN, and even a direct access attempt is blocked with a 403.

### 3.13 DEPT_COORDINATOR — Department Coordinator

**Persona**: Staff member Bae Ji-ho, in the Facilities team, who handles requests coming into the department and onboarding/offboarding subtasks.

Staff member Bae Ji-ho checks a list filtered to show only requests that came into his own department (Facilities) in the **"Department Request Queue"** menu of the sidebar's **"Department Services"** group, clicks an item to go to the detail screen, communicates with the requester via comments, and updates the progress status using the status transition button (in progress/completed/rejected).

When new-hire onboarding is underway, he checks the subtasks assigned to his department (e.g., seat assignment, badge issuance) in the **"My Subtasks"** menu, and once processing is done, clicks the **"Mark Complete"** button. Once every department's subtasks are completed, the overall checklist automatically changes to completed status.

### 3.14 VULNERABILITY_MANAGER — Vulnerability Manager

**Persona**: Senior Associate Hong Yu-jin, who coordinates remediation of vulnerabilities discovered by the security team.

When a vulnerability is found in a security check, Senior Associate Hong Yu-jin registers it in the **"Vulnerability"** menu of the **"Vulnerability"** group, entering a title, discovery date, affected asset/CI, and severity. On the detail screen, she uses the risk score calculation form to enter severity and exploitability to compute a score, and designates an assignee to handle it in the assignment area (it cannot move to the remediation stage until an assignee is designated).

Once the assignee completes the action, she records the action type (patch/configuration change/compensating control) and action date in the remediation registration form, and once verification is done, records pass/fail using the verification result registration button (on pass it automatically moves to the reporting stage; on fail it returns to the remediation stage). If there is a related asset, she links it using the asset/CI link button.

In the **"Vulnerability Metrics"** menu, she checks counts by stage, severity distribution, and average resolution time to review the team's response speed.

### 3.15 COMPLIANCE_OFFICER — Compliance Officer

**Persona**: General Manager Moon Ga-young, in the Legal team, who manages regulatory/policy compliance.

When a new regulation takes effect, General Manager Moon Ga-young registers a requirement in the **"Compliance Requirements"** menu of the **"Compliance"** group, entering a name, basis (regulatory clause or internal policy), and scope of application. On the detail screen, she designates the owner of the requirement in the owner designation area; if there is no owner, it is highlighted as "Owner not assigned" in the list/detail screens.

When a violation case or issue is found, she creates an item by entering details in the corrective action registration form (automatically created in "Detected" status), and while the action is in progress, updates it in the order Detect → In Progress → Resolved using the corrective action status transition button. If there is a related system change, she links it using the change link button, and all activity related to a requirement can be checked in the audit log list.

In the dashboard of the **"Compliance Status"** menu, she checks the overall compliance rate and the number of unresolved corrective actions at a glance to use in management reporting.

### 3.16 INFRA_OPERATOR — Infrastructure Operator

**Persona**: Staff member Jo Eun-woo, who monitors the operational status of servers and network equipment.

At every regular inspection time each day, Staff member Jo Eun-woo selects the target asset in the **"Register Metric"** menu of the **"Infra Monitoring"** group, enters the measurement time and the metric item (uptime/CPU/memory/response time) and value, and clicks the **"Register"** button. If the value falls outside the threshold, a "Threshold exceeded alert has been generated" toast appears immediately upon registration.

When he wants to see metric trends, he selects the asset and period in the **"Metric Dashboard"** menu to check the time-series chart, and compares actual versus target uptime with the SLA-vs-uptime card. When bringing in new equipment and setting a threshold for the first time, he enters the upper/lower bound per metric item in the **"Threshold & Alerts"** menu, and checks generated threshold-exceeded alerts in the list, then marks them as processed with the **"Acknowledge"** button.

For quarterly team capacity planning, he registers the team/service name, capacity, and expected demand in the **"Capacity Planning"** menu to understand utilization, and when management reporting is needed, aggregates average uptime, performance metrics, and capacity utilization by period in the **"Infra Reporting"** menu.
