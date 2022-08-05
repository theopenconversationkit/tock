import {
  AfterViewInit,
  ComponentFactoryResolver,
  Directive,
  ElementRef,
  HostListener,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  ViewContainerRef
} from '@angular/core';
import { Subscription } from 'rxjs';

import { MAX_SCALE, MIN_SCALE, OffsetPosition, Position, ZoomValue, ZOOM_SCALE } from './models';
import { ZoomInOutComponent } from './zoom-in-out/zoom-in-out.component';

const CANVAS_TRANSITION_TIMING = 300;

@Directive({
  selector: '[canvas]'
})
export class CanvasDirective implements OnInit, OnChanges, OnDestroy, AfterViewInit {
  @Input() canvasElement: ElementRef;
  @Input() position: OffsetPosition;
  @Input() centerCanvasAtInitialisation: boolean = true;
  @Input() showZoomAction: boolean = true;

  private canvasPos = { x: 0, y: 0 };
  private canvasPosOffset = { x: 0, y: 0 };
  private pointer = { x: 0, y: 0 };
  private canvasScale: number = 1;
  private zoomSpeed: number = 0.5;
  private isDragingCanvas: Position;

  private wrapper: HTMLElement;
  private canvas: HTMLElement;

  private subscription: Subscription = new Subscription();

  constructor(
    private canvasWrapperElement: ElementRef,
    private viewContainerRef: ViewContainerRef,
    private componentFactoryResolver: ComponentFactoryResolver
  ) {}

  ngOnInit(): void {
    this.wrapper = this.canvasWrapperElement.nativeElement;
    this.wrapper.style.cursor = 'move';
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (
      this.canvas &&
      changes.position.currentValue &&
      changes.position.currentValue !== changes.position.previousValue
    ) {
      this.centerOnElement(changes.position.currentValue);
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  ngAfterViewInit(): void {
    if (this.wrapper.children.length > 1) this.canvas = this.wrapper.querySelector('#canvas');
    else this.canvas = this.wrapper.firstElementChild as HTMLElement;

    if (this.canvas) {
      if (this.centerCanvasAtInitialisation) this.centerCanvas();

      setTimeout(() => {
        if (this.showZoomAction) {
          this.wrapper.style.position = 'relative';
          this.buildZoomActions();
        }
      }, 0);
    } else {
      throw new Error(
        'The container contains several elements. It is necessary to give a "canvas" identifier to the canvas element'
      );
    }
  }

  zoomCanvas(event?: WheelEvent, zoomScale: number = 1): void {
    this.throwErrorUndefinedCanvas();

    this.pointer.x =
      (event?.clientX || this.wrapper.offsetWidth / 2 + this.wrapper.offsetLeft) -
      this.wrapper.offsetLeft;
    this.pointer.y =
      (event?.clientY || this.wrapper.offsetHeight / 2 + this.wrapper.offsetTop) -
      this.wrapper.offsetTop;
    this.canvasPosOffset.x = (this.pointer.x - this.canvasPos.x) / this.canvasScale;
    this.canvasPosOffset.y = (this.pointer.y - this.canvasPos.y) / this.canvasScale;

    const scale = event ? event.deltaY : zoomScale;
    this.canvasScale += -1 * Math.max(-1, Math.min(1, scale)) * this.zoomSpeed * this.canvasScale;

    this.canvasScale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, this.canvasScale));

    this.canvasPos.x = -this.canvasPosOffset.x * this.canvasScale + this.pointer.x;
    this.canvasPos.y = -this.canvasPosOffset.y * this.canvasScale + this.pointer.y;

    this.canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
  }

  /**
   * Create a component with an action button to zoom in/out and add it to the canvas wrapper
   */
  buildZoomActions(): void {
    const componentFactory =
      this.componentFactoryResolver.resolveComponentFactory(ZoomInOutComponent);
    const componentRef = this.viewContainerRef.createComponent(componentFactory);

    this.subscription = componentRef.instance.onZoom.subscribe((v: ZoomValue) => {
      const scale = v === ZoomValue.OUT ? ZOOM_SCALE : -ZOOM_SCALE;
      this.zoomCanvas(null, scale);
    });

    this.wrapper.appendChild(componentRef.location.nativeElement);
  }

  centerCanvas(): void {
    this.throwErrorUndefinedCanvas();

    this.canvasPos.x =
      ((this.canvas.offsetWidth * this.canvasScale - this.wrapper.offsetWidth) / 2) * -1;
    this.canvasPos.y = 0;

    this.canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
  }

  centerOnElement(position: OffsetPosition): void {
    this.throwErrorUndefinedCanvas();

    this.canvasPos.x =
      position.offsetLeft * this.canvasScale * -1 +
      (this.wrapper.offsetWidth - position.offsetWidth) / 2;
    this.canvasPos.y =
      position.offsetTop * this.canvasScale * -1 +
      (this.wrapper.offsetHeight - position.offsetHeight) / 2;

    this.canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
  }

  throwErrorUndefinedCanvas(): void {
    if (!this.canvas) throw new Error('The canvas is not defined');
  }

  @HostListener('wheel', ['$event'])
  public onWheel(event: WheelEvent) {
    event.preventDefault();
    this.throwErrorUndefinedCanvas();

    this.zoomCanvas(event);
  }

  /**
   * Takes the new positions of the canvas and the client
   * @param {MouseEvent} event
   */
  @HostListener('mousedown', ['$event'])
  onMouseDownCanvas(event: MouseEvent): void {
    this.throwErrorUndefinedCanvas();

    if (event.button == 0) {
      this.isDragingCanvas = {
        left: this.canvasPos.x,
        top: this.canvasPos.y,
        clientX: event.clientX,
        clientY: event.clientY
      };
      let canvas = this.canvas;
      canvas.style.transition = 'unset';
    }
  }

  /**
   * Stop the slide of the canvas
   * @param {MouseEvent} event
   */
  @HostListener('mouseup', ['$event'])
  onMouseUpCanvas(event: MouseEvent): void {
    this.throwErrorUndefinedCanvas();

    if (event.button == 0) {
      this.isDragingCanvas = undefined;
      let canvas = this.canvas;
      canvas.style.transition = `transform .${CANVAS_TRANSITION_TIMING}s`;
    }
  }

  /**
   * Change the position of the canvas in the container
   * @param {MouseEvent} event
   */
  @HostListener('mousemove', ['$event'])
  onMouseMoveCanvas(event: MouseEvent): void {
    this.throwErrorUndefinedCanvas();

    if (this.isDragingCanvas && event.button == 0 && this.canvas) {
      let canvas = this.canvas;
      const dx = event.clientX - this.isDragingCanvas.clientX;
      const dy = event.clientY - this.isDragingCanvas.clientY;
      this.canvasPos.x = this.isDragingCanvas.left + dx;
      this.canvasPos.y = this.isDragingCanvas.top + dy;
      canvas.style.transform = `translate(${this.canvasPos.x}px,${this.canvasPos.y}px) scale(${this.canvasScale},${this.canvasScale})`;
    }
  }
}
