document.addEventListener('DOMContentLoaded', function () {
    // Cache DOM elements
    const elements = {
        sidebar: document.querySelector('.sidebar'),
        contentWrapper: document.querySelector('.content-wrapper'),
        sidebarToggle: document.getElementById('sidebarToggle'),
        sidebarToggleTop: document.getElementById('sidebarToggleTop'),
        navItems: document.querySelectorAll('.nav-item .nav-link'),
        dropdowns: {
            notification: {
                trigger: document.getElementById('alertsDropdown'),
                menu: document.querySelector('[aria-labelledby="alertsDropdown"]')
            },
            message: {
                trigger: document.getElementById('messagesDropdown'),
                menu: document.querySelector('[aria-labelledby="messagesDropdown"]')
            },
            user: {
                trigger: document.getElementById('userDropdown'),
                menu: document.querySelector('[aria-labelledby="userDropdown"]')
            }
        },
        scrollToTop: document.querySelector('.scroll-to-top')
    };

    // Sidebar functionality
    class SidebarManager {
        constructor(elements) {
            this.elements = elements;
            this.isMobile = window.innerWidth < 768;
            this.initialize();
        }

        initialize() {
            // Setup sidebar scroll
            if (this.elements.sidebar) {
                this.elements.sidebar.style.height = '100vh';
                this.elements.sidebar.style.overflowY = 'auto';
                this.setupEventListeners();
            }
        }

        setupEventListeners() {
            // Sidebar toggle buttons
            if (this.elements.sidebarToggle) {
                this.elements.sidebarToggle.addEventListener('click', () => this.toggleSidebar());
            }

            if (this.elements.sidebarToggleTop) {
                this.elements.sidebarToggleTop.addEventListener('click', () => this.toggleSidebar());
            }

            // Mobile specific handlers
            this.setupMobileHandlers();

            // Window resize handler
            window.addEventListener('resize', () => this.handleResize());

            // Initial state
            this.handleResize();
        }

        toggleSidebar() {
            this.elements.sidebar.classList.toggle('sidebar-collapsed');
            this.elements.contentWrapper.classList.toggle('content-wrapper-collapsed');

            if (this.elements.sidebarToggle) {
                const icon = this.elements.sidebarToggle.querySelector('i');
                icon.classList.toggle('bi-arrow-left');
                icon.classList.toggle('bi-arrow-right');
            }
        }

        setupMobileHandlers() {
            if (window.innerWidth < 768) {
                this.elements.sidebar.addEventListener('mouseenter', () => this.handleMobileHover(true));
                this.elements.sidebar.addEventListener('mouseleave', () => this.handleMobileHover(false));

                // Close sidebar on mobile when clicking nav items
                this.elements.navItems.forEach(item => {
                    item.addEventListener('click', () => this.handleMobileNavClick());
                });
            }
        }

        handleMobileHover(isEntering) {
            if (this.isMobile) {
                const classes = {
                    sidebar: ['sidebar-mobile-expanded', 'sidebar-collapsed'],
                    content: ['content-wrapper-mobile-shifted', 'content-wrapper-collapsed']
                };

                this.elements.sidebar.classList.toggle(classes.sidebar[0], isEntering);
                this.elements.sidebar.classList.toggle(classes.sidebar[1], !isEntering);
                this.elements.contentWrapper.classList.toggle(classes.content[0], isEntering);
                this.elements.contentWrapper.classList.toggle(classes.content[1], !isEntering);
            }
        }

        handleMobileNavClick() {
            if (this.isMobile && !this.elements.sidebar.classList.contains('sidebar-collapsed')) {
                this.toggleSidebar();
            }
        }

        handleResize() {
            this.isMobile = window.innerWidth < 768;
            if (this.isMobile) {
                this.elements.sidebar.classList.add('sidebar-collapsed');
                this.elements.contentWrapper.classList.add('content-wrapper-collapsed');
            } else {
                this.elements.sidebar.classList.remove('sidebar-collapsed');
                this.elements.contentWrapper.classList.remove('content-wrapper-collapsed');
            }
        }
    }

    // Dropdown Manager
    class DropdownManager {
        constructor(dropdowns) {
            this.dropdowns = dropdowns;
            this.initialize();
        }

        initialize() {
            Object.keys(this.dropdowns).forEach(key => {
                const dropdown = this.dropdowns[key];
                if (dropdown.trigger) {
                    dropdown.trigger.addEventListener('click', (e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        this.toggleDropdown(key);
                    });
                }
            });

            // Close dropdowns when clicking outside
            document.addEventListener('click', (e) => {
                if (!e.target.closest('.dropdown')) {
                    this.closeAllDropdowns();
                }
            });
        }

        toggleDropdown(activeKey) {
            Object.keys(this.dropdowns).forEach(key => {
                const dropdown = this.dropdowns[key];
                const isActive = key === activeKey;

                if (dropdown.menu) {
                    dropdown.menu.classList.toggle('show', isActive);
                }
                if (dropdown.trigger) {
                    dropdown.trigger.setAttribute('aria-expanded', isActive ? 'true' : 'false');
                }
            });
        }

        closeAllDropdowns() {
            Object.values(this.dropdowns).forEach(dropdown => {
                if (dropdown.menu) {
                    dropdown.menu.classList.remove('show');
                }
                if (dropdown.trigger) {
                    dropdown.trigger.setAttribute('aria-expanded', 'false');
                }
            });
        }
    }

    // Chart Manager
    class ChartManager {
        static initialize() {
            if (typeof Chart === 'undefined') return;

            // Area Chart
            const areaChart = document.getElementById('myAreaChart');
            if (areaChart) {
                this.initializeAreaChart(areaChart);
            }

            // Pie Chart
            const pieChart = document.getElementById('myPieChart');
            if (pieChart) {
                this.initializePieChart(pieChart);
            }
        }

        static initializeAreaChart(canvas) {
            new Chart(canvas, {
                type: 'line',
                data: {
                    labels: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"],
                    datasets: [{
                        label: "Nhân viên",
                        lineTension: 0.3,
                        backgroundColor: "rgba(78, 115, 223, 0.05)",
                        borderColor: "rgba(78, 115, 223, 1)",
                        pointRadius: 3,
                        pointBackgroundColor: "rgba(78, 115, 223, 1)",
                        pointBorderColor: "rgba(78, 115, 223, 1)",
                        pointHoverRadius: 3,
                        pointHoverBackgroundColor: "rgba(78, 115, 223, 1)",
                        pointHoverBorderColor: "rgba(78, 115, 223, 1)",
                        pointHitRadius: 10,
                        pointBorderWidth: 2,
                        data: [180, 185, 190, 195, 200, 205, 210, 215, 210, 215, 220, 215],
                    }]
                },
                options: {
                    maintainAspectRatio: false,
                    scales: {
                        x: {
                            grid: {
                                display: false,
                                drawBorder: false
                            },
                            ticks: {
                                maxTicksLimit: 7
                            }
                        },
                        y: {
                            ticks: {
                                maxTicksLimit: 5,
                                padding: 10,
                            },
                            grid: {
                                color: "rgb(234, 236, 244)",
                                zeroLineColor: "rgb(234, 236, 244)",
                                drawBorder: false,
                                borderDash: [2],
                                zeroLineBorderDash: [2]
                            }
                        }
                    },
                    plugins: {
                        legend: {
                            display: false
                        },
                        tooltip: {
                            backgroundColor: "rgb(255,255,255)",
                            bodyColor: "#858796",
                            titleMarginBottom: 10,
                            titleColor: '#6e707e',
                            titleFont: {
                                size: 14
                            },
                            borderColor: '#dddfeb',
                            borderWidth: 1,
                            padding: {
                                x: 15,
                                y: 15
                            },
                            displayColors: false,
                            intersect: false,
                            mode: 'index',
                            caretPadding: 10,
                        }
                    }
                }
            });
        }

        static initializePieChart(canvas) {
            new Chart(canvas, {
                type: 'doughnut',
                data: {
                    labels: ["Kỹ thuật", "Kinh doanh", "Nhân sự", "Kế toán", "Marketing"],
                    datasets: [{
                        data: [55, 30, 15, 10, 8],
                        backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b'],
                        hoverBackgroundColor: ['#2e59d9', '#17a673', '#2c9faf', '#dda20a', '#be2617'],
                        hoverBorderColor: "rgba(234, 236, 244, 1)",
                    }]
                },
                options: {
                    maintainAspectRatio: false,
                    plugins: {
                        tooltip: {
                            backgroundColor: "rgb(255,255,255)",
                            bodyColor: "#858796",
                            borderColor: '#dddfeb',
                            borderWidth: 1,
                            padding: {
                                x: 15,
                                y: 15
                            },
                            displayColors: false,
                            caretPadding: 10,
                        },
                        legend: {
                            display: false
                        }
                    },
                    cutout: '80%'
                }
            });
        }
    }

    // Initialize components
    const sidebarManager = new SidebarManager(elements);
    const dropdownManager = new DropdownManager(elements.dropdowns);
    ChartManager.initialize();

    // Initialize Bootstrap components
    if (typeof bootstrap !== 'undefined') {
        // Initialize dropdowns
        const dropdownTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="dropdown"]'));
        dropdownTriggerList.map(dropdownTriggerEl => new bootstrap.Dropdown(dropdownTriggerEl));

        // Initialize tooltips
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));

        // Initialize popovers
        const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
        popoverTriggerList.map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));
    }

    // Scroll to top functionality
    if (elements.scrollToTop) {
        window.addEventListener('scroll', () => {
            elements.scrollToTop.style.display = window.pageYOffset > 100 ? 'block' : 'none';
        });

        elements.scrollToTop.addEventListener('click', (e) => {
            e.preventDefault();
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    }

    // Initialize DataTables if jQuery is available
    if (typeof $.fn !== 'undefined' && typeof $.fn.DataTable !== 'undefined') {
        const tableIds = [
            'dataTable',
            'allowanceTypeTable',
            'employeeAllowanceTable',
            'bonusTypeTable',
            'employeeBonusTable',
            'insuranceTypeTable',
            'employeeInsuranceTable'
        ];

        tableIds.forEach(tableId => {
            const table = $(`#${tableId}`);
            if (table.length) {
                table.DataTable();
            }
        });
    }
});