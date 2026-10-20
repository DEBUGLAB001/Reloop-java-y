// ReLoop Operations Platform Client Application
const API_BASE = './api';

let appState = {
  overview: null,
  devices: [],
  components: [],
  requirements: [],
  jobs: [],
  stations: [],
  technicians: [],
  activities: [],
  selectedRequirementId: null
};

// DOM ready initialization
document.addEventListener('DOMContentLoaded', () => {
  initNavigation();
  initEventListeners();
  fetchAllData();
});

function initNavigation() {
  const tabs = document.querySelectorAll('.nav-tab');
  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      const targetView = tab.dataset.view;
      navigateTo(targetView);
    });
  });
}

function navigateTo(viewId) {
  document.querySelectorAll('.nav-tab').forEach(t => {
    t.classList.toggle('active', t.dataset.view === viewId);
  });
  document.querySelectorAll('.view-section').forEach(v => {
    v.classList.toggle('active', v.id === viewId);
  });
}

function initEventListeners() {
  document.getElementById('btn-refresh').addEventListener('click', () => {
    showToast('Refreshing system state...', 'info');
    fetchAllData();
  });

  document.getElementById('btn-reset').addEventListener('click', async () => {
    if (!confirm('Reset all demonstration data to baseline deterministic scenario?')) return;
    try {
      const res = await fetch(`${API_BASE}/scenario/reset`, { method: 'POST' });
      if (res.ok) {
        showToast('Scenario data reset to baseline.', 'success');
        fetchAllData();
      } else {
        showToast('Failed to reset scenario.', 'error');
      }
    } catch (e) {
      showToast('Error resetting scenario: ' + e.message, 'error');
    }
  });

  document.getElementById('intake-search').addEventListener('input', renderDevices);
  document.getElementById('intake-status-filter').addEventListener('change', renderDevices);

  document.getElementById('btn-run-prioritization').addEventListener('click', runPrioritizationCycle);
  document.getElementById('btn-batch-allocate-all').addEventListener('click', runBatchAllocation);

  document.getElementById('inspection-form').addEventListener('submit', handleInspectionSubmit);
  document.getElementById('allocation-form').addEventListener('submit', handleAllocationSubmit);
  document.getElementById('scheduling-form').addEventListener('submit', handleScheduleSubmit);
}

// Fetch all resources concurrently
async function fetchAllData() {
  try {
    const [overviewRes, devicesRes, compRes, reqRes, jobsRes, stationsRes, techsRes, actRes] = await Promise.all([
      fetch(`${API_BASE}/overview`).then(r => r.json()),
      fetch(`${API_BASE}/devices`).then(r => r.json()),
      fetch(`${API_BASE}/components`).then(r => r.json()),
      fetch(`${API_BASE}/requirements`).then(r => r.json()),
      fetch(`${API_BASE}/jobs`).then(r => r.json()),
      fetch(`${API_BASE}/stations`).then(r => r.json()),
      fetch(`${API_BASE}/technicians`).then(r => r.json()),
      fetch(`${API_BASE}/activities`).then(r => r.json())
    ]);

    appState.overview = overviewRes;
    appState.devices = devicesRes;
    appState.components = compRes;
    appState.requirements = reqRes;
    appState.jobs = jobsRes;
    appState.stations = stationsRes;
    appState.technicians = techsRes;
    appState.activities = actRes;

    renderAllViews();
  } catch (err) {
    showToast('Error connecting to backend services: ' + err.message, 'error');
  }
}

function renderAllViews() {
  renderOverview();
  renderDevices();
  renderMatching();
  renderPlanning();
  renderAllocations();
  renderScheduling();
  renderActivities();
}

// Render Overview View
function renderOverview() {
  const o = appState.overview;
  if (!o) return;

  document.getElementById('kpi-incoming-devices').textContent = o.incomingDevicesCount;
  document.getElementById('kpi-available-components').textContent = o.availableComponentsCount;
  document.getElementById('kpi-total-components-sub').textContent = `From ${o.totalComponentsCount} total items (${o.reservedComponentsCount} reserved)`;
  document.getElementById('kpi-pending-jobs').textContent = o.pendingJobsCount;
  document.getElementById('kpi-scheduled-jobs').textContent = o.scheduledJobsCount;
  document.getElementById('kpi-completed-jobs-sub').textContent = `${o.completedJobsCount} completed recoveries`;

  // Bay Workloads
  const bayContainer = document.getElementById('overview-station-list');
  bayContainer.innerHTML = (o.stations || []).map(stn => {
    const pct = Math.round((stn.allocatedCapacityUnits / stn.maxCapacityUnits) * 100);
    const colorClass = pct > 100 ? 'fill-rose' : pct > 75 ? 'fill-amber' : 'fill-emerald';
    return `
      <div style="margin-bottom: 1rem;">
        <div style="display:flex; justify-content:space-between; font-size:0.85rem; margin-bottom:0.25rem;">
          <span style="font-weight:600;">${stn.name}</span>
          <span class="mono">${stn.allocatedCapacityUnits} / ${stn.maxCapacityUnits} units (${pct}%)</span>
        </div>
        <div class="progress-bar-container">
          <div class="progress-bar-fill ${colorClass}" style="width: ${Math.min(100, pct)}%;"></div>
        </div>
      </div>
    `;
  }).join('');

  // Overview Activities
  const actContainer = document.getElementById('overview-activity-list');
  actContainer.innerHTML = (o.recentActivities || []).map(a => `
    <div style="padding:0.6rem 0; border-bottom:1px solid rgba(51,65,85,0.3); font-size:0.82rem;">
      <div style="display:flex; justify-content:space-between; color:var(--text-dim); font-size:0.75rem;">
        <span>${formatTime(a.timestamp)}</span>
        <span class="badge badge-teal">${a.action}</span>
      </div>
      <div style="margin-top:0.25rem; color:var(--text-main);">${a.details}</div>
    </div>
  `).join('');
}

// Render Devices View
function renderDevices() {
  const tbody = document.getElementById('device-table-body');
  const search = document.getElementById('intake-search').value.toLowerCase();
  const filter = document.getElementById('intake-status-filter').value;

  const filtered = appState.devices.filter(d => {
    const matchesSearch = d.id.toLowerCase().includes(search) || d.model.toLowerCase().includes(search);
    const matchesFilter = filter === 'ALL' || d.workflowStatus === filter;
    return matchesSearch && matchesFilter;
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-dim); padding:2rem;">No matching devices found.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(d => {
    const statusBadge = getWorkflowBadge(d.workflowStatus);
    const compCount = (d.recoverableComponentIds || []).length;
    return `
      <tr>
        <td class="mono" style="font-weight:600; color:var(--emerald);">${d.id}</td>
        <td style="font-weight:500;">${d.model}</td>
        <td>${d.category}</td>
        <td>${d.condition}</td>
        <td>${statusBadge}</td>
        <td><span class="mono">${compCount}</span> components</td>
        <td>
          ${d.workflowStatus === 'RECEIVED' || d.workflowStatus === 'INSPECTING' ? `
            <button class="btn btn-secondary btn-sm" onclick="openInspectionModal('${d.id}', '${d.model}')">
              Record Inspection
            </button>
          ` : `
            <span style="font-size:0.75rem; color:var(--text-dim);">Inspected</span>
          `}
        </td>
      </tr>
    `;
  }).join('');
}

// Render Matching View
function renderMatching() {
  const reqContainer = document.getElementById('requirements-list');
  reqContainer.innerHTML = appState.requirements.map(r => {
    const isSelected = r.id === appState.selectedRequirementId;
    return `
      <div style="background:${isSelected ? 'rgba(16,185,129,0.1)' : 'rgba(10,15,29,0.5)'}; border:1px solid ${isSelected ? 'var(--emerald)' : 'var(--border-color)'}; border-radius:var(--radius-md); padding:1rem; margin-bottom:0.75rem; cursor:pointer;"
           onclick="selectRequirement('${r.id}')">
        <div style="display:flex; justify-content:space-between; align-items:flex-start;">
          <h4 style="font-size:0.92rem; font-weight:600;">${r.title}</h4>
          ${getUrgencyBadge(r.urgency)}
        </div>
        <div style="display:flex; gap:0.5rem; flex-wrap:wrap; margin-top:0.5rem; font-size:0.78rem;">
          <span class="badge badge-teal">${r.requiredType}</span>
          <span class="badge badge-indigo">Min ${r.minimumCapacity} GB/Wh</span>
          <span class="badge badge-amber">Grade ${r.minimumConditionGrade}+</span>
          <span class="badge badge-purple">${r.targetModelFamily || 'Universal'}</span>
        </div>
        <div style="margin-top:0.6rem; display:flex; justify-content:space-between; align-items:center;">
          <span style="font-size:0.75rem; color:var(--text-dim);">ID: ${r.id}</span>
          <button class="btn btn-primary btn-sm" onclick="event.stopPropagation(); executeRequirementMatch('${r.id}')">
            Match Candidate
          </button>
        </div>
      </div>
    `;
  }).join('');

  // Component Table
  const compTbody = document.getElementById('component-table-body');
  compTbody.innerHTML = appState.components.map(c => {
    const isAvailable = c.availabilityStatus === 'AVAILABLE' && !c.reservedForJobId;
    const statusBadge = isAvailable
      ? `<span class="badge badge-emerald">Available</span>`
      : `<span class="badge badge-amber">Reserved (${c.reservedForJobId || 'Pending'})</span>`;
    return `
      <tr>
        <td class="mono" style="font-weight:600; color:var(--teal);">${c.id}</td>
        <td>${c.componentType}</td>
        <td>${c.manufacturer} / ${c.compatibilityFamily}</td>
        <td class="mono">${c.interfaceType}</td>
        <td class="mono">${c.capacity}</td>
        <td><span class="badge badge-indigo">Grade ${c.conditionGrade}</span></td>
        <td>${statusBadge}</td>
        <td class="mono">${c.sourceDeviceId}</td>
      </tr>
    `;
  }).join('');

  if (appState.selectedRequirementId) {
    inspectRequirement(appState.selectedRequirementId);
  }
}

async function selectRequirement(reqId) {
  appState.selectedRequirementId = reqId;
  renderMatching();
  await inspectRequirement(reqId);
}

async function inspectRequirement(reqId) {
  const req = appState.requirements.find(r => r.id === reqId);
  if (!req) return;

  const inspector = document.getElementById('matching-inspector-content');
  inspector.innerHTML = `<div style="text-align:center; padding:1.5rem;"><span class="spinner"></span> Evaluating candidate compatibility...</div>`;

  try {
    // Evaluate all candidates against this requirement
    const evaluations = await Promise.all(
      appState.components.map(async comp => {
        const res = await fetch(`${API_BASE}/matching/evaluate?candidateId=${comp.id}&requirementId=${req.id}`);
        const data = await res.json();
        return { component: comp, result: data };
      })
    );

    inspector.innerHTML = `
      <div style="margin-bottom:1rem; padding-bottom:0.75rem; border-bottom:1px solid var(--border-color);">
        <h3 style="font-size:0.95rem; font-weight:600;">Evaluating for: ${req.title}</h3>
        <p style="font-size:0.78rem; color:var(--text-dim); margin-top:0.2rem;">
          Target: ${req.requiredType} &bull; Min Capacity: ${req.minimumCapacity} &bull; Min Grade: ${req.minimumConditionGrade}
        </p>
      </div>
      <div style="display:flex; flex-direction:column; gap:0.5rem; max-height:400px; overflow-y:auto;">
        ${evaluations.map(e => {
          const c = e.component;
          const res = e.result;
          return `
            <div style="background:rgba(10,15,29,0.7); border:1px solid ${res.compatible ? 'rgba(16,185,129,0.4)' : 'rgba(244,63,94,0.3)'}; border-radius:var(--radius-sm); padding:0.75rem;">
              <div style="display:flex; justify-content:space-between; align-items:center;">
                <span class="mono" style="font-weight:600; color:${res.compatible ? 'var(--emerald)' : 'var(--text-muted)'};">
                  ${c.id} — ${c.componentType} (${c.manufacturer})
                </span>
                <span class="badge ${res.compatible ? 'badge-emerald' : 'badge-rose'}">
                  ${res.compatible ? `Match (Score: ${res.score})` : 'Incompatible'}
                </span>
              </div>
              <div style="font-size:0.78rem; color:${res.compatible ? 'var(--text-muted)' : 'var(--rose)'}; margin-top:0.35rem;">
                ${res.reason}
              </div>
              <div style="display:flex; gap:0.5rem; font-size:0.72rem; color:var(--text-dim); margin-top:0.35rem;">
                <span>Capacity: ${c.capacity}</span>
                <span>Interface: ${c.interfaceType}</span>
                <span>Grade: ${c.conditionGrade}</span>
                <span>Status: ${c.availabilityStatus}</span>
              </div>
            </div>
          `;
        }).join('')}
      </div>
    `;
  } catch (err) {
    inspector.innerHTML = `<p style="color:var(--rose);">Error evaluating candidates: ${err.message}</p>`;
  }
}

async function executeRequirementMatch(reqId) {
  try {
    const res = await fetch(`${API_BASE}/requirements/${reqId}/match`, { method: 'POST' });
    const data = await res.json();
    if (data.matchedComponent) {
      showToast(`Matched ${data.matchedComponent.id} to requirement!`, 'success');
    } else {
      showToast(`No compatible candidate: ${data.message}`, 'error');
    }
    fetchAllData();
  } catch (e) {
    showToast('Matching error: ' + e.message, 'error');
  }
}

// Render Planning View
function renderPlanning() {
  const tbody = document.getElementById('planning-table-body');
  if (appState.jobs.length === 0) {
    tbody.innerHTML = `<tr><td colspan="10" style="text-align:center; color:var(--text-dim); padding:2rem;">No recovery jobs in queue.</td></tr>`;
    return;
  }

  // Sort by priorityScore descending
  const sorted = [...appState.jobs].sort((a, b) => b.priorityScore - a.priorityScore);

  tbody.innerHTML = sorted.map((job, idx) => {
    const rankBadge = idx === 0 ? `<span class="badge badge-emerald">#1 Priority</span>` : `<span class="mono">#${idx + 1}</span>`;
    return `
      <tr>
        <td>${rankBadge}</td>
        <td class="mono" style="font-weight:600; color:var(--emerald);">${job.id}</td>
        <td>
          <div style="font-weight:600;">${job.title}</div>
          <div style="font-size:0.75rem; color:var(--text-dim);">${job.sourceDeviceId} &bull; ${job.requiredOperation}</div>
        </td>
        <td>${getUrgencyBadge(job.urgency)}</td>
        <td class="mono" style="color:var(--emerald); font-weight:600;">$${job.estimatedRecoveryValue}</td>
        <td class="mono">${job.processingDurationMinutes} min</td>
        <td><span class="badge badge-indigo">${job.requiredTechnicianSkill}</span></td>
        <td class="mono" style="font-size:1rem; font-weight:700; color:var(--teal);">${job.priorityScore.toFixed(1)}</td>
        <td>${getWorkflowBadge(job.lifecycleStatus)}</td>
        <td>
          <div style="display:flex; gap:0.35rem;">
            ${job.lifecycleStatus === 'READY_FOR_RECOVERY' ? `
              <button class="btn btn-secondary btn-sm" onclick="openAllocationModal('${job.id}')" title="Allocate processing bay">
                Allocate
              </button>
            ` : ''}
            ${job.lifecycleStatus !== 'COMPLETED' && job.lifecycleStatus !== 'REJECTED' ? `
              <button class="btn btn-primary btn-sm" onclick="completeJob('${job.id}')" title="Complete recovery job">
                Complete
              </button>
              <button class="btn btn-danger btn-sm" onclick="cancelJob('${job.id}')" title="Cancel job and release resources">
                Cancel
              </button>
            ` : ''}
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

async function runPrioritizationCycle() {
  const btn = document.getElementById('btn-run-prioritization');
  btn.disabled = true;
  btn.innerHTML = `<span class="spinner"></span> Computing Priorities...`;
  try {
    const res = await fetch(`${API_BASE}/jobs/prioritize`, { method: 'POST' });
    const prioritized = await res.json();
    appState.jobs = prioritized;
    renderPlanning();
    showToast('Prioritization cycle executed across all recovery jobs.', 'success');
  } catch (e) {
    showToast('Prioritization error: ' + e.message, 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = `
      <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="5 3 19 12 5 21 5 3"/></svg>
      Run Prioritization Cycle
    `;
  }
}

async function completeJob(jobId) {
  try {
    const res = await fetch(`${API_BASE}/jobs/${jobId}/complete`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ reason: 'Completed recovery operations' })
    });
    if (res.ok) {
      showToast(`Recovery job ${jobId} successfully completed!`, 'success');
      fetchAllData();
    } else {
      const err = await res.json();
      showToast(`Cannot complete job: ${err.error}`, 'error');
    }
  } catch (e) {
    showToast('Error completing job: ' + e.message, 'error');
  }
}

async function cancelJob(jobId) {
  if (!confirm(`Cancel recovery job ${jobId} and release associated resources?`)) return;
  try {
    const res = await fetch(`${API_BASE}/jobs/${jobId}/cancel`, { method: 'POST' });
    if (res.ok) {
      showToast(`Job ${jobId} cancelled. Resources released.`, 'info');
      fetchAllData();
    } else {
      const err = await res.json();
      showToast(`Error cancelling job: ${err.error}`, 'error');
    }
  } catch (e) {
    showToast('Cancellation error: ' + e.message, 'error');
  }
}

// Render Capacity Allocation View
function renderAllocations() {
  const grid = document.getElementById('station-cards-grid');
  grid.innerHTML = appState.stations.map(stn => {
    const pct = Math.round((stn.allocatedCapacityUnits / stn.maxCapacityUnits) * 100);
    const colorClass = pct > 100 ? 'fill-rose' : pct > 75 ? 'fill-amber' : 'fill-emerald';
    return `
      <div class="station-card">
        <div class="station-header">
          <div>
            <h3 style="font-size:0.95rem; font-weight:600;">${stn.name}</h3>
            <span class="mono" style="font-size:0.75rem; color:var(--text-dim);">${stn.id}</span>
          </div>
          <span class="badge ${stn.operationalStatus === 'ONLINE' ? 'badge-emerald' : 'badge-rose'}">
            ${stn.operationalStatus}
          </span>
        </div>
        <div class="station-ops">
          ${Array.from(stn.supportedOperations || []).map(op => `<span class="badge badge-teal">${op}</span>`).join('')}
        </div>
        <div>
          <div class="station-stat">
            <span style="color:var(--text-dim);">Allocated Capacity</span>
            <span class="mono" style="font-weight:600; color:${pct > 100 ? 'var(--rose)' : 'var(--text-main)'};">
              ${stn.allocatedCapacityUnits} / ${stn.maxCapacityUnits} units (${pct}%)
            </span>
          </div>
          <div class="progress-bar-container">
            <div class="progress-bar-fill ${colorClass}" style="width: ${Math.min(100, pct)}%;"></div>
          </div>
        </div>
      </div>
    `;
  }).join('');

  // Unallocated / Ready Jobs Table
  const tbody = document.getElementById('unallocated-jobs-table-body');
  const eligibleJobs = appState.jobs.filter(j => j.lifecycleStatus === 'READY_FOR_RECOVERY');
  if (eligibleJobs.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-dim); padding:1.5rem;">All eligible recovery jobs are allocated.</td></tr>`;
    return;
  }

  tbody.innerHTML = eligibleJobs.map(job => `
    <tr>
      <td class="mono" style="font-weight:600; color:var(--emerald);">${job.id}</td>
      <td style="font-weight:500;">${job.title}</td>
      <td><span class="badge badge-teal">${job.requiredOperation}</span></td>
      <td class="mono">${job.requiredCapacityUnits} units</td>
      <td class="mono" style="font-weight:600;">${job.priorityScore.toFixed(1)}</td>
      <td>${job.assignedStationId ? `<span class="badge badge-indigo">${job.assignedStationId}</span>` : `<span style="color:var(--text-dim);">None</span>`}</td>
      <td>
        <button class="btn btn-secondary btn-sm" onclick="openAllocationModal('${job.id}')">
          Assign Bay
        </button>
      </td>
    </tr>
  `).join('');
}

async function runBatchAllocation() {
  const eligibleJobs = appState.jobs.filter(j => j.lifecycleStatus === 'READY_FOR_RECOVERY');
  if (eligibleJobs.length === 0) {
    showToast('No eligible unallocated jobs found.', 'info');
    return;
  }
  const jobIds = eligibleJobs.map(j => j.id);

  try {
    const res = await fetch(`${API_BASE}/allocations/batch`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ jobIds })
    });
    const results = await res.json();
    const successful = results.filter(r => r.successful).length;
    showToast(`Batch allocation complete: ${successful} / ${results.length} jobs assigned.`, successful > 0 ? 'success' : 'error');
    fetchAllData();
  } catch (e) {
    showToast('Batch allocation error: ' + e.message, 'error');
  }
}

// Render Technician Scheduling View
function renderScheduling() {
  const grid = document.getElementById('technicians-grid');
  grid.innerHTML = appState.technicians.map(t => {
    const assignments = t.scheduledAssignments || [];
    return `
      <div class="card" style="margin-bottom:0;">
        <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:0.75rem;">
          <div>
            <h3 style="font-size:0.95rem; font-weight:600;">${t.name}</h3>
            <span class="mono" style="font-size:0.75rem; color:var(--text-dim);">${t.id}</span>
          </div>
          <span class="badge badge-indigo">Shift: ${t.shiftStart} - ${t.shiftEnd}</span>
        </div>
        <div style="display:flex; flex-wrap:wrap; gap:0.35rem; margin-bottom:0.75rem;">
          ${Array.from(t.skills || []).map(s => `<span class="badge badge-teal">${s}</span>`).join('')}
        </div>
        <div style="font-size:0.8rem; color:var(--text-dim); margin-bottom:0.5rem;">
          Current Scheduled Assignments (${assignments.length}):
        </div>
        <div style="display:flex; flex-direction:column; gap:0.35rem;">
          ${assignments.length === 0 ? `<div style="font-size:0.78rem; color:var(--text-dim);">No active assignments</div>` : ''}
          ${assignments.map(a => `
            <div style="background:rgba(10,15,29,0.7); border:1px solid var(--border-color); border-radius:var(--radius-sm); padding:0.4rem 0.6rem; font-size:0.75rem; display:flex; justify-content:space-between;">
              <span class="mono" style="color:var(--emerald);">${a.jobId}</span>
              <span class="mono">${formatTime(a.startTime)} - ${formatTime(a.endTime)}</span>
            </div>
          `).join('')}
        </div>
      </div>
    `;
  }).join('');

  // Populate Scheduling Form Selectors
  const jobSelect = document.getElementById('schedule-job-select');
  const eligibleJobs = appState.jobs.filter(j => j.lifecycleStatus !== 'COMPLETED' && j.lifecycleStatus !== 'REJECTED');
  jobSelect.innerHTML = eligibleJobs.map(j => `
    <option value="${j.id}">${j.id} — ${j.title} (${j.processingDurationMinutes} min, ${j.requiredTechnicianSkill})</option>
  `).join('');

  const techSelect = document.getElementById('schedule-tech-select');
  techSelect.innerHTML = appState.technicians.map(t => `
    <option value="${t.id}">${t.name} (${Array.from(t.skills || []).join(', ')})</option>
  `).join('');

  // Default time to today 10:00
  const now = new Date();
  const dateStr = now.toISOString().split('T')[0];
  document.getElementById('schedule-start-time').value = `${dateStr}T10:00`;
}

async function handleScheduleSubmit(e) {
  e.preventDefault();
  const jobId = document.getElementById('schedule-job-select').value;
  const technicianId = document.getElementById('schedule-tech-select').value;
  const startTime = document.getElementById('schedule-start-time').value;

  const btn = document.getElementById('btn-submit-schedule');
  btn.disabled = true;

  try {
    const res = await fetch(`${API_BASE}/scheduling/schedule`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ jobId, technicianId, startTime })
    });
    const result = await res.json();
    if (result.successful) {
      showToast(result.message, 'success');
      fetchAllData();
    } else {
      showToast(result.message, 'error');
    }
  } catch (err) {
    showToast('Scheduling error: ' + err.message, 'error');
  } finally {
    btn.disabled = false;
  }
}

// Render Activities
function renderActivities() {
  const tbody = document.getElementById('activity-table-body');
  if (appState.activities.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:var(--text-dim); padding:2rem;">No audit logs recorded.</td></tr>`;
    return;
  }

  tbody.innerHTML = appState.activities.map(a => `
    <tr>
      <td class="mono" style="font-size:0.75rem; color:var(--text-dim);">${formatDateTime(a.timestamp)}</td>
      <td><span class="badge badge-teal">${a.action}</span></td>
      <td>${a.entityType}</td>
      <td class="mono">${a.entityId}</td>
      <td style="font-size:0.82rem;">${a.details}</td>
    </tr>
  `).join('');
}

// Modal Handlers
function openInspectionModal(deviceId, model) {
  document.getElementById('inspect-device-id').value = deviceId;
  document.getElementById('inspection-modal-title').textContent = `Intake Inspection: ${model} (${deviceId})`;
  document.getElementById('inspection-modal').classList.add('active');
}

function openAllocationModal(jobId) {
  document.getElementById('alloc-job-id').value = jobId;
  const select = document.getElementById('alloc-station-select');
  select.innerHTML = appState.stations.map(s => `
    <option value="${s.id}">${s.name} (${s.allocatedCapacityUnits}/${s.maxCapacityUnits} units, ${s.operationalStatus})</option>
  `).join('');
  document.getElementById('allocation-modal').classList.add('active');
}

function closeModal(modalId) {
  document.getElementById(modalId).classList.remove('active');
}

async function handleInspectionSubmit(e) {
  e.preventDefault();
  const deviceId = document.getElementById('inspect-device-id').value;
  const condition = document.getElementById('inspect-condition').value;
  const notes = document.getElementById('inspect-notes').value;

  try {
    const res = await fetch(`${API_BASE}/devices/${deviceId}/inspect`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ condition, notes, components: [] })
    });
    if (res.ok) {
      showToast(`Device ${deviceId} inspection completed!`, 'success');
      closeModal('inspection-modal');
      fetchAllData();
    } else {
      const err = await res.json();
      showToast(`Inspection failed: ${err.error}`, 'error');
    }
  } catch (err) {
    showToast('Inspection submission error: ' + err.message, 'error');
  }
}

async function handleAllocationSubmit(e) {
  e.preventDefault();
  const jobId = document.getElementById('alloc-job-id').value;
  const stationId = document.getElementById('alloc-station-select').value;

  try {
    const res = await fetch(`${API_BASE}/allocations/job`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ jobId, stationId })
    });
    const result = await res.json();
    if (result.successful) {
      showToast(result.message, 'success');
      closeModal('allocation-modal');
      fetchAllData();
    } else {
      showToast(result.message, 'error');
    }
  } catch (err) {
    showToast('Allocation error: ' + err.message, 'error');
  }
}

// Helpers
function getWorkflowBadge(status) {
  switch (status) {
    case 'RECEIVED': return `<span class="badge badge-teal">Received</span>`;
    case 'INSPECTING': return `<span class="badge badge-amber">Inspecting</span>`;
    case 'READY_FOR_RECOVERY': return `<span class="badge badge-emerald">Ready</span>`;
    case 'SCHEDULED': return `<span class="badge badge-indigo">Scheduled</span>`;
    case 'IN_RECOVERY': return `<span class="badge badge-purple">In Recovery</span>`;
    case 'RECOVERED': return `<span class="badge badge-emerald">Recovered</span>`;
    case 'COMPLETED': return `<span class="badge badge-emerald">Completed</span>`;
    case 'REJECTED': return `<span class="badge badge-rose">Rejected</span>`;
    default: return `<span class="badge badge-teal">${status}</span>`;
  }
}

function getUrgencyBadge(urgency) {
  switch (urgency) {
    case 'CRITICAL': return `<span class="badge badge-rose">Critical</span>`;
    case 'HIGH': return `<span class="badge badge-amber">High</span>`;
    case 'MEDIUM': return `<span class="badge badge-indigo">Medium</span>`;
    case 'LOW': return `<span class="badge badge-teal">Low</span>`;
    default: return `<span class="badge">${urgency}</span>`;
  }
}

function formatTime(isoStr) {
  if (!isoStr) return '--:--';
  const parts = isoStr.split('T');
  return parts.length > 1 ? parts[1].substring(0, 5) : isoStr;
}

function formatDateTime(isoStr) {
  if (!isoStr) return '';
  return isoStr.replace('T', ' ').substring(0, 19);
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transition = 'opacity 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}
