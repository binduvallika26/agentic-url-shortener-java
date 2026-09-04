const $ = selector => document.querySelector(selector);
let scenario = 'greenfield';
let run = null;
let currentLinkCode = null;
const examples = {
  greenfield: {requirement: 'Build a secure URL shortener with custom aliases, expiration, visit analytics, audit logging, and safe release controls.', help: 'Greenfield: describe a new system or feature to build.'},
  brownfield: {requirement: 'Enhance the existing URL shortener with expiring links and visit analytics without breaking current redirects.', help: 'Brownfield: describe an enhancement, refactor, or defect in the existing service.'},
  ambiguous: {requirement: 'Make links smart.', help: 'Ambiguous: intentionally omit details to demonstrate the human clarification gate.'}
};
const headers = () => ({'Content-Type': 'application/json', 'X-Actor': ($('#actor').value.trim() || 'anonymous')});
const api = async (url, options = {}) => { const response = await fetch(url, options); if (!response.ok) { const error = await response.json(); throw Error(error.detail || error.title || response.statusText); } return response.status === 204 ? null : response.json(); };
const toast = message => { const element = $('#toast'); element.textContent = message; element.classList.add('show'); setTimeout(() => element.classList.remove('show'), 3200); };

document.querySelectorAll('[data-scenario]').forEach(button => button.onclick = () => {
  document.querySelectorAll('[data-scenario]').forEach(item => item.classList.remove('active'));
  button.classList.add('active'); scenario = button.dataset.scenario;
  $('#requirement').value = examples[scenario].requirement; $('#scenarioHelp').textContent = examples[scenario].help;
});

async function capabilities() { const value = await api('/api/capabilities'); $('#mode').textContent = `● ${value.llm.mode} · ${value.llm.provider}`; $('#knowledgeCount').textContent = `${value.rag.indexedChunks} standards indexed · RAG ON`; }
function renderRun() {
  if (!run) return;
  $('#runStatus').textContent = run.status; $('#runStatus').className = `status-pill ${run.status.toLowerCase()}`; $('#runSubtitle').textContent = `${run.scenario} · revision ${run.revision} · ${run.id.slice(0, 8)}`;
  $('#graph').innerHTML = Object.values(run.graph).map(step => { const state = run.steps[step.id]; const dependency = step.dependsOn.length ? `after ${step.dependsOn.join(', ')}` : 'entry node'; return `<div class="node ${state.status.toLowerCase()}"><b>${step.agent.replaceAll('-', ' ')}</b><small>${state.status.replaceAll('_', ' ')}</small><small>${dependency}</small></div>`; }).join('');
  const waiting = Object.values(run.steps).find(step => step.status === 'AWAITING_APPROVAL');
  $('#advance').disabled = ['SUCCEEDED', 'ROLLED_BACK', 'SAFE_STOPPED'].includes(run.status); $('#approve').disabled = !waiting; $('#approve').dataset.step = waiting?.id || ''; $('#approve').textContent = waiting?.id === 'requirements' ? 'Approve clarified requirement' : 'Approve release gate'; $('#replan').disabled = false; $('#rollback').disabled = !Object.values(run.steps).some(step => step.status === 'SUCCEEDED');
  $('#runGuidance').textContent = waiting?.id === 'requirements' ? 'Paused by design: clarify the requirement, click Replan, then approve the clarified requirement and execute again.' : waiting?.id === 'release' ? 'Paused by design: review the evidence, approve the release gate, then execute once more.' : run.status === 'SUCCEEDED' ? 'All dependency gates completed successfully.' : 'Execute the ready agents to advance this workflow.'; renderEvidence();
}
const artifactLabels = {"product-readme": "Product scope and setup", architecture: "Architecture and control flow", "link-service": "URL-shortener implementation", "url-policy": "Security URL policy", "api-tests": "Integration tests", "test-report": "Maven/Surefire test report", "coverage-report": "JaCoCo coverage summary", "demo-guide": "Interview demo guide", "build-contract": "Maven build contract"};
const escapeHtml = value => String(value).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&#039;');
const renderArtifacts = artifacts => artifacts.map(id => artifactLabels[id] ? `<a class="artifact-link" href="/api/artifacts/${encodeURIComponent(id)}" target="_blank">${escapeHtml(artifactLabels[id])}</a>` : `<span>${escapeHtml(id)}</span>`).join(' · ');
function renderEvidence() {
  const items = Object.values(run.steps).filter(step => step.result); const element = $('#evidenceList');
  if (!items.length) { element.className = 'feed empty'; element.textContent = 'Agents have not produced evidence yet.'; return; }
  element.className = 'feed'; element.innerHTML = items.map(step => `<div class="event"><b>${escapeHtml(step.id.toUpperCase())} · ${escapeHtml(step.result.mode)}${step.fallbackUsed ? ' · FALLBACK' : ''}</b><p>${escapeHtml(step.result.summary)}</p><p>Evidence: ${step.result.evidence.map(escapeHtml).join(' · ')}</p><p>Risks: ${step.result.risks.map(escapeHtml).join(' · ')}</p><p>Artifacts: ${renderArtifacts(step.result.artifacts)}</p></div>`).join('');
}
function formatMetric(key, value) { if (key === 'successRate' || key === 'rollbackFrequency') return `${Math.round(value * 100)}%`; if (key === 'meanEndToEndLatencyMs') return value >= 1000 ? `${(value / 1000).toFixed(1)}s` : `${Math.round(value)}ms`; return Math.round(value); }
async function refresh() {
  const [metrics, audit] = await Promise.all([api('/api/metrics'), api('/api/audit')]); const labels = {totalRuns: 'Total runs', successRate: 'Successful completion', replans: 'Replans', retries: 'Retry attempts', fallbacks: 'Fallback steps', rollbackFrequency: 'Runs rolled back', meanEndToEndLatencyMs: 'Mean end-to-end latency'}; const order = ['totalRuns', 'successRate', 'replans', 'retries', 'fallbacks', 'rollbackFrequency', 'meanEndToEndLatencyMs'];
  $('#metrics').innerHTML = order.map(key => `<div class="metric"><b>${formatMetric(key, metrics[key])}</b><small>${labels[key]}</small></div>`).join(''); const element = $('#audit'); if (audit.length) { element.className = 'feed'; element.innerHTML = audit.slice(0, 12).map(event => `<div class="event"><b>${event.action} · ${event.outcome}</b><p>${event.actor} · ${new Date(event.occurredAt).toLocaleTimeString()} · ${event.correlationId.slice(0, 8)}</p></div>`).join(''); } if (currentLinkCode) await refreshLinkStats();
}
async function refreshLinkStats() { try { const link = await api(`/api/links/${currentLinkCode}`); $('#linkResult').innerHTML = `Created <a style="color:#d7ff69" href="/${link.code}" target="_blank">${location.origin}/${link.code}</a> · visits ${link.visits}`; } catch (_) { currentLinkCode = null; } }
function validRequirement() { const value = $('#requirement').value.trim(); if (!value) { toast('Enter an engineering requirement before launching.'); return false; } if (/^https?:\/\/\S+$/i.test(value)) { toast('Describe the software change here. Enter destination URLs in the URL Product Demo below.'); return false; } return true; }

$('#launch').onclick = async () => { if (!validRequirement()) return; try { run = await api('/api/workflows', {method: 'POST', headers: headers(), body: JSON.stringify({scenario, requirement: $('#requirement').value.trim()})}); renderRun(); refresh(); toast('Workflow created with governed boundaries'); } catch (error) { toast(error.message); } };
$('#simulate').onclick = () => { $('#requirement').value = 'Enhance the existing URL service safely [simulate-development-failure]'; scenario = 'brownfield'; document.querySelectorAll('[data-scenario]').forEach(item => item.classList.toggle('active', item.dataset.scenario === scenario)); $('#scenarioHelp').textContent = 'Fallback demo: only Development will exhaust two attempts and use the safe fallback.'; toast('Development failure marker added — one fallback will activate'); };
$('#advance').onclick = async () => { try { run = await api(`/api/workflows/${run.id}/advance`, {method: 'POST', headers: headers()}); renderRun(); refresh(); toast(run.status === 'AWAITING_APPROVAL' ? 'Paused at a human approval gate' : 'Ready agents executed'); } catch (error) { toast(error.message); } };
$('#approve').onclick = async () => { try { const step = $('#approve').dataset.step; run = await api(`/api/workflows/${run.id}/steps/${step}/approve`, {method: 'POST', headers: headers(), body: JSON.stringify({reason: 'Accountable reviewer approved validated evidence'})}); renderRun(); refresh(); toast(`${step} gate approved; click Execute ready agents to continue`); } catch (error) { toast(error.message); } };
$('#replan').onclick = async () => { if (!validRequirement()) return; try { run = await api(`/api/workflows/${run.id}/replan`, {method: 'POST', headers: headers(), body: JSON.stringify({requirement: $('#requirement').value.trim()})}); renderRun(); refresh(); toast(`Revision ${run.revision} created; approve Requirements, then execute again`); } catch (error) { toast(error.message); } };
$('#rollback').onclick = async () => { try { run = await api(`/api/workflows/${run.id}/rollback`, {method: 'POST', headers: headers(), body: JSON.stringify({reason: 'Reviewer requested safe rollback'})}); renderRun(); refresh(); toast('Rollback completed and audited'); } catch (error) { toast(error.message); } };
$('#shorten').onclick = async () => { try { const link = await api('/api/links', {method: 'POST', headers: headers(), body: JSON.stringify({url: $('#longUrl').value, customCode: $('#alias').value})}); currentLinkCode = link.code; await refreshLinkStats(); refresh(); toast('Short link persisted; visit count refreshes when you return'); } catch (error) { toast(error.message); } };
window.addEventListener('focus', () => { refresh(); });
capabilities().then(refresh); setInterval(refresh, 8000);
