(() => {
  const data = window.dashboardData || {};

  const money = (value) =>
    new Intl.NumberFormat("es-PE", {
      style: "currency",
      currency: "PEN",
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(Number(value || 0));

  const renderRevenueChart = () => {
    const el = document.querySelector("#dashboardRevenueChart");
    const points = Array.isArray(data.monthlyRevenue) ? data.monthlyRevenue : [];

    if (!el || !window.ApexCharts) return;

    const labels = points.map((point) => point.month || "");
    const series = points.map((point) => Number(point.amount || 0));

    new ApexCharts(el, {
      series: [{ name: "Ingresos", data: series }],
      chart: {
        type: "area",
        height: 240,
        toolbar: { show: false },
        zoom: { enabled: false },
        fontFamily: "var(--bs-body-font-family)",
        parentHeightOffset: 0,
      },
      colors: ["var(--bs-primary)"],
      stroke: {
        curve: "smooth",
        width: 3,
        colors: ["var(--bs-primary)"],
      },
      fill: {
        type: "gradient",
        gradient: {
          shadeIntensity: 0.2,
          opacityFrom: 0.35,
          opacityTo: 0.05,
          stops: [0, 90, 100],
        },
      },
      dataLabels: { enabled: false },
      markers: {
        size: 4,
        colors: ["var(--bs-primary)"],
        strokeColors: "#fff",
        strokeWidth: 2,
      },
      xaxis: {
        categories: labels,
        axisBorder: { show: false },
        axisTicks: { show: false },
        labels: {
          style: {
            colors: "var(--bs-body-color)",
            fontFamily: "var(--bs-body-font-family)",
          },
        },
      },
      yaxis: {
        labels: {
          formatter: (value) => money(value),
          style: {
            colors: "var(--bs-body-color)",
            fontFamily: "var(--bs-body-font-family)",
          },
        },
      },
      grid: {
        borderColor: "var(--bs-border-color)",
        strokeDashArray: 4,
      },
      tooltip: {
        y: {
          formatter: (value) => money(value),
        },
      },
    }).render();
  };

  const renderStatusChart = () => {
    const el = document.querySelector("#dashboardStatusChart");
    const points = Array.isArray(data.orderStatus) ? data.orderStatus : [];

    if (!el || !window.ApexCharts) return;

    const labels = points.map((point) => point.status || "");
    const series = points.map((point) => Number(point.count || 0));
    const total = series.reduce((acc, value) => acc + value, 0);
    const effectiveLabels = total > 0 ? labels : ["Sin datos"];
    const effectiveSeries = total > 0 ? series : [1];

    new ApexCharts(el, {
      series: effectiveSeries,
      labels: effectiveLabels,
      chart: {
        type: "donut",
        height: 220,
        toolbar: { show: false },
        fontFamily: "var(--bs-body-font-family)",
        parentHeightOffset: 0,
      },
      colors: [
        "var(--bs-primary)",
        "var(--bs-warning)",
        "var(--bs-info)",
        "var(--bs-success)",
        "var(--bs-danger)",
        "var(--bs-secondary)",
        "rgba(var(--bs-primary-rgb), 0.25)",
      ],
      dataLabels: { enabled: false },
      legend: {
        position: "bottom",
        fontFamily: "var(--bs-body-font-family)",
        labels: {
          colors: "var(--bs-body-color)",
        },
      },
      plotOptions: {
        pie: {
          donut: {
            size: "72%",
          },
        },
      },
      tooltip: {
        y: {
          formatter: (value) => `${value} pedidos`,
        },
      },
    }).render();
  };

  document.addEventListener("DOMContentLoaded", () => {
    renderRevenueChart();
    renderStatusChart();
  });
})();
