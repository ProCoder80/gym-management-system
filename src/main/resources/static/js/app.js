/**
 * Minimal Vanilla JS for Titan Gym Management System
 * Handles Live AJAX Fee Preview & Modal Dialogs
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Live Fee Calculation Preview on /enroll
    initFeePreview();

    // 2. Modal dialog helper listeners
    initModals();

    // 3. Auto-populate edit modal on members page
    initMemberEdit();
});

function initFeePreview() {
    const planSelect = document.getElementById('enrollPlanType');
    const lockerCheckbox = document.getElementById('enrollLocker');
    const dietCheckbox = document.getElementById('enrollDietPlan');
    const ptInput = document.getElementById('enrollPtSessions');
    const enrollmentTypeRadios = document.querySelectorAll('input[name="enrollmentType"]');

    if (!planSelect) return; // Not on the enrollment page

    const updatePreview = () => {
        const plan = planSelect.value || 'MONTHLY';
        const locker = lockerCheckbox ? lockerCheckbox.checked : false;
        const diet = dietCheckbox ? dietCheckbox.checked : false;
        const pt = ptInput ? parseInt(ptInput.value, 10) || 0 : 0;
        
        let enrollmentType = 'NEW';
        enrollmentTypeRadios.forEach(radio => {
            if (radio.checked) enrollmentType = radio.value;
        });

        const url = `/api/fee-preview?planType=${encodeURIComponent(plan)}&locker=${locker}&dietPlan=${diet}&ptSessions=${pt}&enrollmentType=${enrollmentType}`;

        fetch(url)
            .then(res => res.json())
            .then(data => {
                document.getElementById('prevBaseFee').innerText = `$${data.baseFee.toFixed(2)}`;
                document.getElementById('prevLockerFee').innerText = `$${data.lockerFee.toFixed(2)}`;
                document.getElementById('prevDietFee').innerText = `$${data.dietPlanFee.toFixed(2)}`;
                document.getElementById('prevPtFee').innerText = `$${data.ptSessionsFee.toFixed(2)}`;
                document.getElementById('prevDiscount').innerText = `-$${data.discount.toFixed(2)}`;
                document.getElementById('prevTotalFee').innerText = `$${data.totalFee.toFixed(2)}`;
                document.getElementById('prevStrategyName').innerText = data.strategyName;
                document.getElementById('prevDescription').innerText = data.description;

                // Sync strategy hidden field in form
                const stratField = document.getElementById('selectedStrategyName');
                if (stratField) stratField.value = data.strategyName;
            })
            .catch(err => console.error('Error fetching live fee preview:', err));
    };

    planSelect.addEventListener('change', updatePreview);
    if (lockerCheckbox) lockerCheckbox.addEventListener('change', updatePreview);
    if (dietCheckbox) dietCheckbox.addEventListener('change', updatePreview);
    if (ptInput) {
        ptInput.addEventListener('input', updatePreview);
        ptInput.addEventListener('change', updatePreview);
    }
    enrollmentTypeRadios.forEach(r => r.addEventListener('change', updatePreview));

    // Initial trigger on load
    updatePreview();
}

function initModals() {
    // Open modal buttons
    document.querySelectorAll('[data-open-modal]').forEach(btn => {
        btn.addEventListener('click', () => {
            const modalId = btn.getAttribute('data-open-modal');
            const modal = document.getElementById(modalId);
            if (modal) modal.classList.add('active');
        });
    });

    // Close modal buttons
    document.querySelectorAll('[data-close-modal]').forEach(btn => {
        btn.addEventListener('click', () => {
            const modal = btn.closest('.modal-overlay');
            if (modal) modal.classList.remove('active');
        });
    });

    // Close when clicking outside modal dialog
    window.addEventListener('click', (e) => {
        if (e.target.classList.contains('modal-overlay')) {
            e.target.classList.remove('active');
        }
    });
}

function initMemberEdit() {
    document.querySelectorAll('.btn-edit-member').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            const name = btn.getAttribute('data-name');
            const email = btn.getAttribute('data-email');
            const phone = btn.getAttribute('data-phone');
            const status = btn.getAttribute('data-status');

            document.getElementById('editMemberId').value = id;
            document.getElementById('editMemberName').value = name;
            document.getElementById('editMemberEmail').value = email;
            document.getElementById('editMemberPhone').value = phone;
            document.getElementById('editMemberStatus').value = status;

            const modal = document.getElementById('editMemberModal');
            if (modal) modal.classList.add('active');
        });
    });
}
